/***
 * Copyright (c) 2009-2019 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("Non-Profit OSL" 3.0)
 * License text at https://github.com/jflamy/owlcms4/blob/master/LICENSE.txt
 */
package app.owlcms.ui.preparation;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.slf4j.LoggerFactory;
import org.vaadin.crudui.crud.CrudListener;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import app.owlcms.components.fields.BodyWeightField;
import app.owlcms.components.fields.LocalDateField;
import app.owlcms.data.athlete.Athlete;
import app.owlcms.data.athlete.AthleteRepository;
import app.owlcms.data.athlete.Gender;
import app.owlcms.data.category.AgeDivision;
import app.owlcms.data.category.MastersAgeGroup;
import app.owlcms.data.category.Category;
import app.owlcms.data.category.CategoryRepository;
import app.owlcms.data.competition.Competition;
import app.owlcms.data.group.Group;
import app.owlcms.data.group.GroupRepository;
import app.owlcms.ui.crudui.OwlcmsCrudFormFactory;
import app.owlcms.ui.crudui.OwlcmsCrudGrid;
import app.owlcms.ui.crudui.OwlcmsGridLayout;
import app.owlcms.ui.shared.AppLayoutAware;
import app.owlcms.ui.shared.AthleteRegistrationFormFactory;
import app.owlcms.ui.shared.ContentWrapping;
import app.owlcms.ui.shared.OwlcmsRouterLayout;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Class AthleteContent
 *
 * Defines the toolbar and the table for editing data on athletes.
 *
 */
@SuppressWarnings("serial")
@Route(value = "preparation/athletes", layout = RegistrationLayout.class)
@HtmlImport("frontend://styles/shared-styles.html")
public class RegistrationContent extends VerticalLayout
		implements CrudListener<Athlete>, ContentWrapping, AppLayoutAware, HasDynamicTitle {

	final static Logger logger = (Logger) LoggerFactory.getLogger(RegistrationContent.class);
	static {
		logger.setLevel(Level.INFO);
	}

	private TextField lastNameFilter = new TextField();
	private ComboBox<AgeDivision> ageDivisionFilter = new ComboBox<>();
	private ComboBox<Category> categoryFilter = new ComboBox<>();
	private ComboBox<Group> groupFilter = new ComboBox<>();
	private ComboBox<Boolean> weighedInFilter = new ComboBox<>();
	private ComboBox<String> ageGroupFilter = new ComboBox<>();
	private OwlcmsRouterLayout routerLayout;
	private OwlcmsCrudGrid<Athlete> crudGrid;
	private OwlcmsCrudFormFactory<Athlete> crudFormFactory;
	

	/**
	 * Instantiates the athlete crudGrid
	 */
	public RegistrationContent() {
		crudFormFactory = createFormFactory();
		crudGrid = createCrudGrid(crudFormFactory);
		defineFilters(crudGrid);
		fillHW(crudGrid, this);
	}

	/**
	 * The columns of the crudGrid
	 *
	 * @param crudFormFactory what to call to create the form for editing an athlete
	 * @return
	 */
	protected OwlcmsCrudGrid<Athlete> createCrudGrid(OwlcmsCrudFormFactory<Athlete> crudFormFactory) {
		Grid<Athlete> grid = new Grid<>(Athlete.class, false);
		grid.addColumn("lotNumber").setHeader("Lot");
		grid.addColumn("lastName").setHeader("Last Name");
		grid.addColumn("firstName").setHeader("First Name");
		grid.addColumn("team").setHeader("Team");
		grid.addColumn("yearOfBirth").setHeader("Birth");
		grid.addColumn("gender").setHeader("Gender");
		grid.addColumn("ageDivision").setHeader("Age Division");
		if (Competition.getCurrent().isMasters()) {
			grid.addColumn("mastersAgeGroup").setHeader("Age Group");
		}
		grid.addColumn("category").setHeader("Category");
		grid.addColumn(
			new NumberRenderer<>(Athlete::getBodyWeight, "%.2f", this.getLocale()),"bodyWeight")
			.setHeader("Body Weight");
		grid.addColumn("group").setHeader("Group");
		grid.addColumn("eligibleForIndividualRanking").setHeader("Eligible");
		OwlcmsCrudGrid<Athlete> crud = new OwlcmsCrudGrid<>(
				Athlete.class,
				new OwlcmsGridLayout(Athlete.class),
				crudFormFactory,
				grid);
		crud.setCrudListener(this);
		crud.setClickRowToUpdate(true);
		return crud;
	}

	/**
	 * Define the form used to edit a given athlete.
	 *
	 * @return the form factory that will create the actual form on demand
	 */
	protected OwlcmsCrudFormFactory<Athlete> createFormFactory() {
		OwlcmsCrudFormFactory<Athlete> athleteEditingFormFactory = new AthleteRegistrationFormFactory(Athlete.class);
		createFormLayout(athleteEditingFormFactory);
		return athleteEditingFormFactory;
	}

	/**
	 * The content and ordering of the editing form
	 *
	 * @param crudFormFactory the factory that will create the form using this information
	 */
	private void createFormLayout(OwlcmsCrudFormFactory<Athlete> crudFormFactory) {
		List<String> props = new LinkedList<>();
		List<String> captions = new LinkedList<>();
		
		props.add("lastName"); captions.add("Last Name");
		props.add("firstName"); captions.add("First Name");
		props.add("gender"); captions.add("Gender");

		props.add("team"); captions.add("Team");
		props.add("fullBirthDate"); captions.add("Birth Date (yyyy-mm-dd)");
		if (Competition.getCurrent().isMasters()) {
			props.add("mastersAgeGroup"); captions.add("Age Group");
		} else {
			props.add("ageDivision"); captions.add("Age Division");
		}
		props.add("category"); captions.add("Category");
		props.add("group"); captions.add("Group");
		props.add("qualifyingTotal"); captions.add("Entry Total");
		props.add("bodyWeight"); captions.add("Body Weight");
		props.add("snatch1Declaration"); captions.add("Snatch Decl.");
		props.add("cleanJerk1Declaration"); captions.add("C&J Decl.");
		props.add("eligibleForIndividualRanking"); captions.add("Eligible for Individual Ranking?"); 
		props.add("lotNumber"); captions.add("Lot");
		crudFormFactory.setVisibleProperties((String[]) props.toArray(new String[0]));
		crudFormFactory.setFieldCaptions((String[]) captions.toArray(new String[0]));
		
		crudFormFactory.setFieldProvider("gender",
			new ComboBoxProvider<>(
					"Gender", Arrays.asList(Gender.values()), new TextRenderer<>(Gender::name), Gender::name));
		crudFormFactory.setFieldProvider("group",
			new ComboBoxProvider<>(
					"Group", GroupRepository.findAll(), new TextRenderer<>(Group::getName), Group::getName));
		crudFormFactory.setFieldProvider("category",
			new ComboBoxProvider<>(
					"Category", CategoryRepository.findActive(), new TextRenderer<>(Category::getName),
					Category::getName));
		crudFormFactory.setFieldProvider("ageDivision",
			new ComboBoxProvider<>(
					"AgeDivision", Arrays.asList(AgeDivision.values()), new TextRenderer<>(AgeDivision::name),
					AgeDivision::name));

		crudFormFactory.setFieldType("bodyWeight", BodyWeightField.class);
		crudFormFactory.setFieldType("fullBirthDate", LocalDateField.class);
	}

	@Override
	public Athlete add(Athlete Athlete) {
		crudFormFactory.add(Athlete);
		return Athlete;
	}

	@Override
	public Athlete update(Athlete Athlete) {
		return crudFormFactory.update(Athlete);
	}

	@Override
	public void delete(Athlete Athlete) {
		crudFormFactory.delete(Athlete);
		return;
	}


	/**
	 * The refresh button on the toolbar; also called by refreshGrid when the group is changed.
	 *
	 * @see org.vaadin.crudui.crud.CrudListener#findAll()
	 */
	@Override
	public Collection<Athlete> findAll() {
		List<Athlete> all = AthleteRepository
			.findFiltered(lastNameFilter.getValue(), groupFilter.getValue(), categoryFilter.getValue(),
				ageDivisionFilter.getValue(), weighedInFilter.getValue(), -1, -1);
		return doExtraFiltering(all);
	}

	public Collection<Athlete> doFindAll(EntityManager em) {
		List<Athlete> all = AthleteRepository.doFindFiltered(em, lastNameFilter.getValue(), groupFilter.getValue(),
				categoryFilter.getValue(), ageDivisionFilter.getValue(), weighedInFilter.getValue(), -1, -1);
		return doExtraFiltering(all);
	}

	private Collection<Athlete> doExtraFiltering(List<Athlete> all) {
		String filterValue = ageGroupFilter != null ? ageGroupFilter.getValue() : null;
		if (filterValue == null) {
			return all;
		} else {
			List<Athlete> some = all.stream().filter(a -> a.getMastersAgeGroup().startsWith(filterValue))
					.collect(Collectors.toList());
			return some;
		}
	}

	/**
	 * The filters at the top of the crudGrid
	 *
	 * @param crudGrid the crudGrid that will be filtered.
	 */
	protected void defineFilters(OwlcmsCrudGrid<Athlete> crudGrid) {
		lastNameFilter.setPlaceholder("Last name");
		lastNameFilter.setClearButtonVisible(true);
		lastNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
		lastNameFilter.addValueChangeListener(e -> {
			crudGrid.refreshGrid();
		});
		lastNameFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(lastNameFilter);

		ageDivisionFilter.setPlaceholder("Age Division");
		ageDivisionFilter.setItems(AgeDivision.findAll());
		ageDivisionFilter.setItemLabelGenerator(AgeDivision::name);
		ageDivisionFilter.addValueChangeListener(e -> {
			crudGrid.refreshGrid();
		});
		lastNameFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(ageDivisionFilter);
		
		if (Competition.getCurrent().isMasters()) {
			ageGroupFilter.setPlaceholder("Age Group");
			ageGroupFilter.setItems(MastersAgeGroup.findAllStrings());
//		ageGroupFilter.setItemLabelGenerator(AgeDivision::name);
			ageGroupFilter.addValueChangeListener(e -> {
				crudGrid.refreshGrid();
			});
			ageGroupFilter.setWidth("10em");
			crudGrid.getCrudLayout().addFilterComponent(ageGroupFilter);
		}

		categoryFilter.setPlaceholder("Category");
		categoryFilter.setItems(CategoryRepository.findActive());
		categoryFilter.setItemLabelGenerator(Category::getName);
		categoryFilter.addValueChangeListener(e -> {
			crudGrid.refreshGrid();
		});
		categoryFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(categoryFilter);

		groupFilter.setPlaceholder("Group");
		groupFilter.setItems(GroupRepository.findAll());
		groupFilter.setItemLabelGenerator(Group::getName);
		groupFilter.addValueChangeListener(e -> {
			crudGrid.refreshGrid();
		});
		groupFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(groupFilter);

		weighedInFilter.setPlaceholder("Weighed-In?");
		weighedInFilter.setItems(Boolean.TRUE,Boolean.FALSE);
		weighedInFilter.setItemLabelGenerator((i) -> {return i ? "Weighed" : "Not weighed";});
		weighedInFilter.addValueChangeListener(e -> {
			crudGrid.refreshGrid();
		});
		weighedInFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(weighedInFilter);

		Button clearFilters = new Button(null, VaadinIcon.ERASER.create());
		clearFilters.addClickListener(event -> {
			lastNameFilter.clear();
			ageDivisionFilter.clear();
			categoryFilter.clear();
			groupFilter.clear();
			weighedInFilter.clear();
		});
		lastNameFilter.setWidth("10em");
		crudGrid.getCrudLayout().addFilterComponent(clearFilters);
	}

	/* (non-Javadoc)
	 * @see app.owlcms.ui.shared.AppLayoutAware#getRouterLayout() */
	@Override
	public OwlcmsRouterLayout getRouterLayout() {
		return routerLayout;
	}

	/* (non-Javadoc)
	 * @see
	 * app.owlcms.ui.shared.AppLayoutAware#setRouterLayout(app.owlcms.ui.shared.OwlcmsRouterLayout) */
	@Override
	public void setRouterLayout(OwlcmsRouterLayout routerLayout) {
		this.routerLayout = routerLayout;
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		getRouterLayout().closeDrawer();
	}

	public void refreshCrudGrid() {
		crudGrid.refreshGrid();
	}
	
	/**
	 * @see com.vaadin.flow.router.HasDynamicTitle#getPageTitle()
	 */
	@Override
	public String getPageTitle() {
		return "Preparation - Registration";
	}
}
