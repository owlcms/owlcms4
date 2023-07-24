/*******************************************************************************
 * Copyright (c) 2009-2023 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("NPOSL-3.0")
 * License text at https://opensource.org/licenses/NPOSL-3.0
 *******************************************************************************/
package app.owlcms.nui.preparation;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.slf4j.LoggerFactory;
import org.vaadin.crudui.crud.CrudListener;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;

import app.owlcms.apputils.queryparameters.FOPParameters;
import app.owlcms.components.ConfirmationDialog;
import app.owlcms.components.GroupSelectionMenu;
import app.owlcms.data.agegroup.AgeGroup;
import app.owlcms.data.agegroup.AgeGroupRepository;
import app.owlcms.data.athlete.Athlete;
import app.owlcms.data.athlete.AthleteRepository;
import app.owlcms.data.athlete.Gender;
import app.owlcms.data.athleteSort.AthleteSorter;
import app.owlcms.data.category.AgeDivision;
import app.owlcms.data.category.Category;
import app.owlcms.data.category.CategoryRepository;
import app.owlcms.data.group.Group;
import app.owlcms.data.group.GroupRepository;
import app.owlcms.data.jpa.JPAService;
import app.owlcms.data.platform.Platform;
import app.owlcms.i18n.Translator;
import app.owlcms.init.OwlcmsSession;
import app.owlcms.nui.crudui.OwlcmsCrudFormFactory;
import app.owlcms.nui.crudui.OwlcmsCrudGrid;
import app.owlcms.nui.crudui.OwlcmsGridLayout;
import app.owlcms.nui.shared.NAthleteRegistrationFormFactory;
import app.owlcms.nui.shared.OwlcmsContent;
import app.owlcms.nui.shared.OwlcmsLayout;
import app.owlcms.utils.NaturalOrderComparator;
import app.owlcms.utils.URLUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Class RegistrationContent
 *
 * Defines the toolbar and the table for editing registration data on athletes.
 *
 */
@SuppressWarnings("serial")
@Route(value = "preparation/athletes", layout = OwlcmsLayout.class)
@CssImport(value = "./styles/shared-styles.css")
public class RegistrationContent extends VerticalLayout implements CrudListener<Athlete>, OwlcmsContent, FOPParameters {

	final static Logger logger = (Logger) LoggerFactory.getLogger(RegistrationContent.class);
	static {
		logger.setLevel(Level.INFO);
	}

	private ComboBox<AgeDivision> ageDivisionFilter = new ComboBox<>();
	private ComboBox<AgeGroup> ageGroupFilter = new ComboBox<>();
	private ComboBox<Category> categoryFilter = new ComboBox<>();
	protected OwlcmsCrudGrid<Athlete> crudGrid;
	private Group group;
	protected ComboBox<Gender> genderFilter = new ComboBox<>();
	private ComboBox<Group> groupFilter = new ComboBox<>();
	protected TextField lastNameFilter = new TextField();
	private Location location;
	private UI locationUI;
	private OwlcmsLayout routerLayout;

	private ComboBox<Boolean> weighedInFilter = new ComboBox<>();
//    private Group group;
	private ComboBox<Group> groupSelect;
	protected GroupSelectionMenu topBarMenu;

	protected FlexLayout topBar;

	Map<String, List<String>> urlParameterMap = new HashMap<>();
	private Category category;
	private Gender gender;
	private Platform platform;
	private AgeDivision ageDivision;
	private String ageGroupPrefix;
	private String lastName;
	private AgeGroup ageGroup;
	private Boolean weighedIn;

	/**
	 * Instantiates the athlete crudGrid
	 */
	public RegistrationContent() {
		init();
	}

	@Override
	public Athlete add(Athlete athlete) {
		if (athlete.getGroup() == null && getGroup() != null) {
			athlete.setGroup(getGroup());
		}
		((OwlcmsCrudFormFactory<Athlete>) crudGrid.getCrudFormFactory()).add(athlete);
		return athlete;
	}

	@Override
	public FlexLayout createMenuArea() {
		createTopBarGroupSelect();

		Button drawLots = new Button(getTranslation("DrawLotNumbers"), (e) -> {
			drawLots();
		});

		Button deleteAthletes = new Button(getTranslation("DeleteAthletes"), (e) -> {
			new ConfirmationDialog(getTranslation("DeleteAthletes"), getTranslation("Warning_DeleteAthletes"),
			        getTranslation("Done_period"), () -> {
				        deleteAthletes();
			        }).open();

		});
		deleteAthletes.getElement().setAttribute("title", getTranslation("DeleteAthletes_forListed"));

		Button clearLifts = new Button(getTranslation("ClearLifts"), (e) -> {
			new ConfirmationDialog(getTranslation("ClearLifts"), getTranslation("Warning_ClearAthleteLifts"),
			        getTranslation("LiftsCleared"), () -> {
				        clearLifts();
			        }).open();
		});
		deleteAthletes.getElement().setAttribute("title", getTranslation("ClearLifts_forListed"));

		Button resetCats = new Button(getTranslation("ResetCategories.ResetAthletes"), (e) -> {
			new ConfirmationDialog(
			        getTranslation("ResetCategories.ResetCategories"),
			        getTranslation("ResetCategories.Warning_ResetCategories"),
			        getTranslation("ResetCategories.CategoriesReset"), () -> {
				        resetCategories();
			        }).open();
		});
		resetCats.getElement().setAttribute("title", getTranslation("ResetCategories.ResetCategoriesMouseOver"));

		HorizontalLayout buttons;
		buttons = new HorizontalLayout(drawLots, deleteAthletes, clearLifts,
		        resetCats);

		buttons.setPadding(false);
		buttons.setMargin(false);
		buttons.setSpacing(true);
		buttons.setAlignItems(FlexComponent.Alignment.BASELINE);

		topBar = new FlexLayout();
		topBar.getStyle().set("flex", "100 1");
		topBar.removeAll();
		topBar.add(topBarMenu, buttons);
		topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		topBar.setAlignItems(FlexComponent.Alignment.CENTER);

		return topBar;
	}

	@Override
	public void delete(Athlete athlete) {
		((OwlcmsCrudFormFactory<Athlete>) crudGrid.getCrudFormFactory()).delete(athlete);
		return;
	}

	/**
	 * The refresh button on the toolbar; also called by refreshGrid when the group
	 * is changed.
	 *
	 * @see org.vaadin.crudui.crud.CrudListener#findAll()
	 */
	@Override
	public Collection<Athlete> findAll() {
		List<Athlete> findFiltered = AthleteRepository.findFiltered(getLastName(), getGroup(),
		        getCategory(), getAgeGroup(), getAgeDivision(),
		        getGender(), getWeighedIn(), -1, -1);
		AthleteSorter.registrationOrder(findFiltered);
		updateURLLocations();
		return findFiltered;
	}

	/**
	 * @return the groupFilter
	 */
	public ComboBox<Group> getGroupFilter() {
		return groupFilter;
	}

	public ComboBox<Group> getGroupSelect() {
		return groupSelect;
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public UI getLocationUI() {
		return this.locationUI;
	}

	@Override
	public String getMenuTitle() {
		return getPageTitle();
	}

	/**
	 * @see com.vaadin.flow.router.HasDynamicTitle#getPageTitle()
	 */
	@Override
	public String getPageTitle() {
		return getTranslation("EditRegisteredAthletes");
	}

	@Override
	public OwlcmsLayout getRouterLayout() {
		return routerLayout;
	}

	@Override
	public Map<String, List<String>> getUrlParameterMap() {
		return urlParameterMap;
	}

	@Override
	public boolean isIgnoreFopFromURL() {
		return true;
	}

	@Override
	public boolean isIgnoreGroupFromURL() {
		return false;
	}

	public void refresh() {
		crudGrid.refreshGrid();
	}

	public void refreshCrudGrid() {
		crudGrid.refreshGrid();
	}

	@Override
	public void setHeaderContent() {
		routerLayout.setMenuTitle(getPageTitle());
		routerLayout.setMenuArea(createMenuArea());
		routerLayout.showLocaleDropdown(false);
		routerLayout.setDrawerOpened(false);
		routerLayout.updateHeader(true);
	}

	@Override
	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public void setLocationUI(UI locationUI) {
		this.locationUI = locationUI;
	}

	/**
	 * Parse the http query parameters
	 *
	 * Note: because we have the @Route, the parameters are parsed *before* our
	 * parent layout is created.
	 *
	 * @param event     Vaadin navigation event
	 * @param parameter null in this case -- we don't want a vaadin "/" parameter.
	 *                  This allows us to add query parameters instead.
	 *
	 * @see app.owlcms.apputils.queryparameters.FOPParameters#setParameter(com.vaadin.flow.router.BeforeEvent,
	 *      java.lang.String)
	 */
	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		setLocation(event.getLocation());
		setLocationUI(event.getUI());
		QueryParameters queryParameters = getLocation().getQueryParameters();
		Map<String, List<String>> parametersMap = queryParameters.getParameters(); // immutable
		HashMap<String, List<String>> params = new HashMap<>(parametersMap);

		// logger.trace("parsing query parameters RegistrationContent");
		List<String> groupNames = params.get("group");
		// logger.trace("groupNames = {}", groupNames);
		if (!isIgnoreGroupFromURL() && groupNames != null && !groupNames.isEmpty()) {
			String groupName = groupNames.get(0);
			groupName = URLDecoder.decode(groupName, StandardCharsets.UTF_8);
			setGroup(GroupRepository.findByName(groupName));
		} else {
			setGroup(null);
		}
		if (getGroup() != null) {
			params.put("group", Arrays.asList(URLUtils.urlEncode(getGroup().getName())));
			OwlcmsCrudFormFactory<Athlete> crudFormFactory = createFormFactory();
			crudGrid.setCrudFormFactory(crudFormFactory);
		} else {
			params.remove("group");
		}

		params.remove("fop");

		// change the URL to reflect group
		event.getUI().getPage().getHistory().replaceState(null,
		        new Location(getLocation().getPath(), new QueryParameters(URLUtils.cleanParams(params))));
	}

	@Override
	public void setRouterLayout(OwlcmsLayout routerLayout) {
		this.routerLayout = routerLayout;
	}

	@Override
	public void setUrlParameterMap(Map<String, List<String>> newParameterMap) {
		this.urlParameterMap = newParameterMap;
	}

	@Override
	public Athlete update(Athlete athlete) {
		OwlcmsSession.setAttribute("weighIn", athlete);
		Athlete a = ((OwlcmsCrudFormFactory<Athlete>) crudGrid.getCrudFormFactory()).update(athlete);
		OwlcmsSession.setAttribute("weighIn", null);
		return a;
	}

	/**
	 * The columns of the crudGrid
	 *
	 * @param crudFormFactory what to call to create the form for editing an athlete
	 * @return
	 */
	protected OwlcmsCrudGrid<Athlete> createCrudGrid(OwlcmsCrudFormFactory<Athlete> crudFormFactory) {
		Grid<Athlete> grid = new Grid<>(Athlete.class, false);

		grid.getThemeNames().add("row-stripes");
		grid.getThemeNames().add("compact");
		grid.addColumn("lotNumber").setHeader(getTranslation("Lot")).setAutoWidth(true)
		        .setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn("lastName").setHeader(getTranslation("LastName")).setWidth("20ch");
		grid.addColumn("firstName").setHeader(getTranslation("FirstName"));
		grid.addColumn("team").setHeader(getTranslation("Team")).setAutoWidth(true);
		grid.addColumn("yearOfBirth").setHeader(getTranslation("BirthDate")).setAutoWidth(true)
		        .setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn("gender").setHeader(getTranslation("Gender")).setAutoWidth(true)
		        .setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn("ageGroup").setHeader(getTranslation("AgeGroup")).setAutoWidth(true)
		        .setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn("category").setHeader(getTranslation("Category")).setAutoWidth(true)
		        .setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn(new NumberRenderer<>(Athlete::getBodyWeight, "%.2f", this.getLocale()))
		        .setSortProperty("bodyWeight")
		        .setHeader(getTranslation("BodyWeight")).setAutoWidth(true).setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn("group").setHeader(getTranslation("Group")).setAutoWidth(true)
		        .setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn("eligibleCategories").setHeader(getTranslation("Registration.EligibleCategories"))
		        .setAutoWidth(true);
		grid.addColumn("entryTotal").setHeader(getTranslation("EntryTotal")).setAutoWidth(true)
		        .setTextAlign(ColumnTextAlign.CENTER);
		grid.addColumn("federationCodes").setHeader(getTranslation("Registration.FederationCodesShort"))
		        .setAutoWidth(true);

		OwlcmsCrudGrid<Athlete> crudGrid = new OwlcmsCrudGrid<>(Athlete.class, new OwlcmsGridLayout(Athlete.class) {

			@Override
			public void hideForm() {
				// registration should be the same as weigh-in (set an attribute to prevent
				// interference with validations)
				super.hideForm();
				logger.trace("clearing {}", OwlcmsSession.getAttribute("weighIn"));
				OwlcmsSession.setAttribute("weighIn", null);
			}
		},
		        crudFormFactory, grid);
		crudGrid.setCrudListener(this);
		crudGrid.setClickRowToUpdate(true);
		return crudGrid;
	}

	/**
	 * Define the form used to edit a given athlete.
	 *
	 * @return the form factory that will create the actual form on demand
	 */
	protected OwlcmsCrudFormFactory<Athlete> createFormFactory() {
		OwlcmsCrudFormFactory<Athlete> athleteEditingFormFactory;
		athleteEditingFormFactory = new NAthleteRegistrationFormFactory(Athlete.class,
		        group);
		// createFormLayout(athleteEditingFormFactory);
		return athleteEditingFormFactory;
	}

	protected void createTopBarGroupSelect() {
		// there is already all the SQL filtering logic for the group attached
		// hidden field in the crudGrid part of the page so we just set that
		// filter.

		List<Group> groups = GroupRepository.findAll();
		groups.sort(new NaturalOrderComparator<Group>());

		OwlcmsSession.withFop(fop -> {
			// logger.debug("initial setting group to {} {}", getCurrentGroup(),
			// LoggerUtils.whereFrom());
			getGroupFilter().setValue(getGroup());
			// switching to group "*" is understood to mean all groups
			topBarMenu = new GroupSelectionMenu(groups, getGroup(),
			        fop,
			        (g1) -> doSwitchGroup(g1),
			        (g1) -> doSwitchGroup(new Group("*")),
			        null,
			        Translator.translate("AllGroups"));
		});
	}

	/**
	 * The filters at the top of the crudGrid
	 *
	 * @param crudGrid the crudGrid that will be filtered.
	 */

	protected void defineFilters(OwlcmsCrudGrid<Athlete> crudGrid) {
		lastNameFilter.setPlaceholder(getTranslation("LastName"));
		lastNameFilter.setClearButtonVisible(true);
		lastNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
		lastNameFilter.addValueChangeListener(e -> {
			setLastName(e.getValue());
			crudGrid.refreshGrid();
		});
		lastNameFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(lastNameFilter);

		ageDivisionFilter.setPlaceholder(getTranslation("AgeDivision"));
		ageDivisionFilter.setItems(AgeDivision.findAll());
		ageDivisionFilter.setItemLabelGenerator((ad) -> getTranslation("Division." + ad.name()));
		ageDivisionFilter.setClearButtonVisible(true);
		ageDivisionFilter.addValueChangeListener(e -> {
			setAgeDivision(e.getValue());
			crudGrid.refreshGrid();
		});
		ageDivisionFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(ageDivisionFilter);

		ageGroupFilter.setPlaceholder(getTranslation("AgeGroup"));
		ageGroupFilter.setItems(AgeGroupRepository.findAll());
		// ageGroupFilter.setItemLabelGenerator(AgeDivision::name);
		ageGroupFilter.setClearButtonVisible(true);
		ageGroupFilter.addValueChangeListener(e -> {
			setAgeGroup(e.getValue());
			crudGrid.refreshGrid();
		});
		ageGroupFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(ageGroupFilter);

		categoryFilter.setPlaceholder(getTranslation("Category"));
		categoryFilter.setItems(CategoryRepository.findActive());
		categoryFilter.setItemLabelGenerator(Category::getTranslatedName);
		categoryFilter.setClearButtonVisible(true);
		categoryFilter.addValueChangeListener(e -> {
			setCategory(e.getValue());
			crudGrid.refreshGrid();
		});
		categoryFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(categoryFilter);

		groupFilter.setPlaceholder(getTranslation("Group"));
		List<Group> groups = GroupRepository.findAll();
		groups.sort(new NaturalOrderComparator<Group>());
		groupFilter.setItems(groups);
		groupFilter.setItemLabelGenerator(Group::getName);
		groupFilter.addValueChangeListener(e -> {
			crudGrid.refreshGrid();
			setGroup(e.getValue());
			updateURLLocation(getLocationUI(), getLocation(), e.getValue());
		});
		groupFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(groupFilter);
		groupFilter.getStyle().set("display", "none");

		weighedInFilter.setPlaceholder(getTranslation("Weighed_in_p"));
		weighedInFilter.setItems(Boolean.TRUE, Boolean.FALSE);
		weighedInFilter.setItemLabelGenerator((i) -> {
			return i ? getTranslation("Weighed") : getTranslation("Not_weighed");
		});
		weighedInFilter.setClearButtonVisible(true);
		weighedInFilter.addValueChangeListener(e -> {
			setWeighedIn(e.getValue());
			crudGrid.refreshGrid();
		});
		weighedInFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(weighedInFilter);

		genderFilter.setPlaceholder(getTranslation("Gender"));
		genderFilter.setItems(Gender.M, Gender.F);
		genderFilter.setItemLabelGenerator((i) -> {
			return i == Gender.M ? getTranslation("Gender.Men") : getTranslation("Gender.Women");
		});
		genderFilter.setClearButtonVisible(true);
		genderFilter.addValueChangeListener(e -> {
			setGender(e.getValue());
			crudGrid.refreshGrid();
		});
		genderFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(genderFilter);

		Button clearFilters = new Button(null, VaadinIcon.CLOSE.create());
		clearFilters.addClickListener(event -> {
			lastNameFilter.clear();
			ageGroupFilter.clear();
			ageDivisionFilter.clear();
			categoryFilter.clear();
			// groupFilter.clear();
			weighedInFilter.clear();
			genderFilter.clear();
		});
		lastNameFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(clearFilters);
	}

	protected void errorNotification() {
		Label content = new Label(getTranslation("Select_group_first"));
		content.getElement().setAttribute("theme", "error");
		Button buttonInside = new Button(getTranslation("GotIt"));
		buttonInside.getElement().setAttribute("theme", "error primary");
		VerticalLayout verticalLayout = new VerticalLayout(content, buttonInside);
		verticalLayout.setAlignItems(Alignment.CENTER);
		Notification notification = new Notification(verticalLayout);
		notification.setDuration(3000);
		buttonInside.addClickListener(event -> notification.close());
		notification.setPosition(Position.MIDDLE);
		notification.open();
	}

	/**
	 * @return the ageDivision
	 */
	protected AgeDivision getAgeDivision() {
		return ageDivision;
	}

	protected AgeGroup getAgeGroup() {
		return ageGroup;
	}

	/**
	 * @return the ageGroupPrefix
	 */
	protected String getAgeGroupPrefix() {
		return ageGroupPrefix;
	}

	protected Category getCategory() {
		return category;
	}

	protected Category getCategoryValue() {
		return getCategory();
	}

	protected Group getGroup() {
		return group;
	}

	protected Gender getGender() {
		return gender;
	}

	protected String getLastName() {
		return lastName;
	}

	protected Platform getPlatform() {
		return platform;
	}

	protected Boolean getWeighedIn() {
		return weighedIn;
	}

	protected void init() {
		OwlcmsCrudFormFactory<Athlete> crudFormFactory = createFormFactory();
		crudGrid = createCrudGrid(crudFormFactory);
		defineFilters(crudGrid);
		fillHW(crudGrid, this);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
	}

	protected void setAgeDivision(AgeDivision ageDivision) {
		this.ageDivision = ageDivision;
	}

	protected void setAgeGroup(AgeGroup value) {
		this.ageGroup = value;

	}

	protected void setAgeGroupPrefix(String ageGroupPrefix) {
		this.ageGroupPrefix = ageGroupPrefix;
	}

	protected void setCategory(Category category) {
		this.category = category;
	}

	protected void setGroup(Group currentGroup) {
		this.group = currentGroup;
	}

	protected void setGender(Gender value) {
		this.gender = value;
	}

	/**
	 * @param groupSelect the groupSelect to set
	 */
	protected void setGroupSelect(ComboBox<Group> groupSelect) {
		this.groupSelect = groupSelect;
	}

	protected void setLastName(String value) {
		this.lastName = value;

	}

	protected void setPlatform(Platform platformValue) {
		this.platform = platformValue;
	}

	protected void updateURLLocations() {
		updateURLLocation(UI.getCurrent(), getLocation(), "fop", null);

		String ag = getAgeGroupPrefix() != null ? getAgeGroupPrefix() : null;
		updateURLLocation(UI.getCurrent(), getLocation(), "ag",
		        ag);
		String ad = getAgeDivision() != null ? getAgeDivision().name() : null;
		updateURLLocation(UI.getCurrent(), getLocation(), "ad",
		        ad);
		String cat = getCategoryValue() != null ? getCategoryValue().getComputedCode() : null;
		updateURLLocation(UI.getCurrent(), getLocation(), "cat",
		        cat);
		String platformName = getPlatform() != null ? getPlatform().getName() : null;
		// logger.debug("updating platform {}", platformName);
		updateURLLocation(UI.getCurrent(), getLocation(), "platform",
		        platformName);
		String group = getGroup() != null ? getGroup().getName() : null;
		updateURLLocation(UI.getCurrent(), getLocation(), "group",
		        group);
		String gender = getGender() != null ? getGender().name() : null;
		updateURLLocation(UI.getCurrent(), getLocation(), "gender",
		        gender);
	}

	private void clearLifts() {
		JPAService.runInTransaction(em -> {
			List<Athlete> athletes = (List<Athlete>) doFindAll(em);
			for (Athlete a : athletes) {
				a.clearLifts();
				em.merge(a);
			}
			em.flush();
			return null;
		});
	}

	private void deleteAthletes() {
		JPAService.runInTransaction(em -> {
			List<Athlete> athletes = (List<Athlete>) doFindAll(em);
			for (Athlete a : athletes) {
				em.remove(a);
			}
			em.flush();
			return null;
		});
		refreshCrudGrid();
	}

	private Collection<Athlete> doFindAll(EntityManager em) {
		List<Athlete> all = AthleteRepository.doFindFiltered(em, lastNameFilter.getValue(), groupFilter.getValue(),
		        categoryFilter.getValue(), ageGroupFilter.getValue(), ageDivisionFilter.getValue(),
		        genderFilter.getValue(), weighedInFilter.getValue(), -1, -1);
		return all;
	}

	private void doSwitchGroup(Group newCurrentGroup) {
		if (newCurrentGroup != null && newCurrentGroup.getName() == "*") {
			setGroup(null);
		} else {
			setGroup(newCurrentGroup);
		}
		getRouterLayout().updateHeader(true);
		getGroupFilter().setValue(newCurrentGroup);
	}

	private void drawLots() {
		JPAService.runInTransaction(em -> {
			List<Athlete> toBeShuffled = AthleteRepository.doFindAll(em);
			AthleteSorter.drawLots(toBeShuffled);
			for (Athlete a : toBeShuffled) {
				em.merge(a);
			}
			em.flush();
			return null;
		});
		refreshCrudGrid();
	}

	private void resetCategories() {
		AthleteRepository.resetParticipations();
		refreshCrudGrid();
	}

	private void setWeighedIn(Boolean value) {
		this.weighedIn = value;
	}

	private void updateURLLocation(UI ui, Location location, Group newGroup) {
		// change the URL to reflect fop group
		HashMap<String, List<String>> params = new HashMap<>(
		        location.getQueryParameters().getParameters());
		if (!isIgnoreGroupFromURL() && newGroup != null) {
			params.put("group", Arrays.asList(URLUtils.urlEncode(newGroup.getName())));
			if (newGroup != null) {
				params.put("group", Arrays.asList(URLUtils.urlEncode(newGroup.getName())));
				setGroup(newGroup);
				OwlcmsCrudFormFactory<Athlete> crudFormFactory = createFormFactory();
				crudGrid.setCrudFormFactory(crudFormFactory);
			}
		} else {
			params.remove("group");
		}
		ui.getPage().getHistory().replaceState(null,
		        new Location(location.getPath(), new QueryParameters(URLUtils.cleanParams(params))));
	}

}
