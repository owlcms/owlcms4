/*******************************************************************************
 * Copyright (c) 2009-2023 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("NPOSL-3.0")
 * License text at https://opensource.org/licenses/NPOSL-3.0
 *******************************************************************************/

package app.owlcms.nui.referee;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterListener;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;

import app.owlcms.apputils.SoundUtils;
import app.owlcms.apputils.queryparameters.BaseContent;
import app.owlcms.apputils.queryparameters.FOPParametersReader;
import app.owlcms.components.elements.BeepElement;
import app.owlcms.fieldofplay.FOPEvent;
import app.owlcms.fieldofplay.FieldOfPlay;
import app.owlcms.i18n.Translator;
import app.owlcms.init.OwlcmsFactory;
import app.owlcms.init.OwlcmsSession;
import app.owlcms.nui.lifting.UIEventProcessor;
import app.owlcms.nui.shared.RequireLogin;
import app.owlcms.nui.shared.SafeEventBusRegistration;
import app.owlcms.simulation.CompetitionSimulator;
import app.owlcms.uievents.UIEvent;
import app.owlcms.utils.LoggerUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Class AnnouncerContent.
 */
@SuppressWarnings({ "serial", "deprecation" })
@Route(value = "ref")
@CssImport(value = "./styles/shared-styles.css")

public class RefContent extends BaseContent implements FOPParametersReader, SafeEventBusRegistration,
        UIEventProcessor, HasDynamicTitle, RequireLogin, BeforeEnterListener {

	private class DelayTimer {
		private final Timer t = new Timer();

		public TimerTask schedule(final Runnable r, long delay) {
			final TimerTask task = new TimerTask() {
				@Override
				public void run() {
					r.run();
				}
			};
			this.t.schedule(task, delay);
			return task;
		}
	}

	final private static Logger logger = (Logger) LoggerFactory.getLogger(RefContent.class);
	private static final String REF_INDEX = "num";
	final private static Logger uiEventLogger = (Logger) LoggerFactory.getLogger("UI" + logger.getName());

	static {
		logger.setLevel(Level.INFO);
		uiEventLogger.setLevel(Level.INFO);
	}
	private Icon bad;
	private BeepElement beeper;
	private Icon good;
	private Location location;
	private UI locationUI;
	private boolean redTouched;
	private Integer ref13ix = null; // 1 2 or 3
	private IntegerField refField;
	private HorizontalLayout refVotingButtons;
	private VerticalLayout refVotingCenterHorizontally;
	private HorizontalLayout topRow;
	private EventBus uiEventBus;
	private Map<String, List<String>> urlParams;
	private HorizontalLayout warningRow;
	private HorizontalLayout juryRow;
	private boolean whiteTouched;
	Map<String, List<String>> urlParameterMap = new HashMap<>();
	private HorizontalLayout topWrapper;

	public RefContent() {
		OwlcmsFactory.waitDBInitialized();
		init();
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		RequireLogin.super.beforeEnter(event);
		UI.getCurrent().getPage().setTitle(getPageTitle());
	}

	/**
	 * @see com.vaadin.flow.router.HasDynamicTitle#getPageTitle()
	 */
	@Override
	public String getPageTitle() {
		return Translator.translate("Referee") + OwlcmsSession.getFopNameIfMultiple()
		        + (getRef13ix() != null ? (" " + getRef13ix()) : "");
	}

	/**
	 * Parse the http query parameters
	 *
	 * Note: because we have the @Route, the parameters are parsed *before* our parent layout is created.
	 *
	 * @param event     Vaadin navigation event
	 * @param parameter null in this case -- we don't want a vaadin "/" parameter. This allows us to add query parameters instead.
	 *
	 * @see app.owlcms.apputils.queryparameters.FOPParameters#setParameter(com.vaadin.flow.router.BeforeEvent, java.lang.String)
	 */
	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		this.location = event.getLocation();
		this.locationUI = event.getUI();
		QueryParameters queryParameters = this.location.getQueryParameters();
		Map<String, List<String>> parametersMap = queryParameters.getParameters(); // immutable
		this.urlParams = readParams(this.location, parametersMap);

		// get the referee number from query parameters, do not add value if num is not
		// defined
		List<String> nums = parametersMap.get(REF_INDEX);
		String num = null;
		if (nums != null) {
			num = nums.get(0);
			try {
				setRef13ix(Integer.parseInt(num));
				logger.debug("parsed {} parameter = {}", REF_INDEX, num);
				this.refField.setValue(getRef13ix().intValue());
			} catch (NumberFormatException e) {
				setRef13ix(null);
				num = null;
				LoggerUtils.logError(logger, e);
			}
		}

	}

	@Subscribe
	public void slaveDecision(UIEvent.Decision e) {
		UIEventProcessor.uiAccess(this, this.uiEventBus, () -> {
			this.good.getElement().setEnabled(false); // cannot grant after down has been given
			this.redTouched = false; // re-enable processing of red.
		});
	}

	/**
	 * This must come from a timer on FieldOfPlay, because if we are using mobile devices there will not be a master decision reset coming from the
	 * keypad-hosting device
	 *
	 * @param e
	 */
	@Subscribe
	public void slaveDecisionReset(UIEvent.DecisionReset e) {
		logger.debug("received decision reset {}", getRef13ix());
		UIEventProcessor.uiAccess(this, this.uiEventBus, () -> {
			resetRefVote();
		});
	}

	@Subscribe
	public void slaveDown(UIEvent.DownSignal e) {
	}

	@Subscribe
	public void slaveRefereeUpdate(UIEvent.RefereeUpdate e) {
		// only used during simulations to show what the fake referees pressed.
		if (!CompetitionSimulator.isRunning()) {
			return;
		}
		UIEventProcessor.uiAccessIgnoreIfSelfOrigin(this, this.uiEventBus, e, this, () -> {
			switch (getRef13ix()) {
				case 1:
					if (e.ref1 != null) {
						if (e.ref1) {
							doWhiteColor();
						} else {
							doRedColor();
						}
					}
					break;
				case 2:
					if (e.ref2 != null) {
						if (e.ref2) {
							doWhiteColor();
						} else {
							doRedColor();
						}
					}
					break;
				case 3:
					if (e.ref3 != null) {
						if (e.ref3) {
							doWhiteColor();
						} else {
							doRedColor();
						}
					}
					break;
				default:
					break;
			}
		});

	}

	@Subscribe
	public void slaveStartLifting(UIEvent.StartLifting e) {
		logger.debug("received decision reset {}", getRef13ix());
		UIEventProcessor.uiAccess(this, this.uiEventBus, () -> {
			resetRefVote();
		});
	}

	@Subscribe
	public void slaveSummonRef(UIEvent.SummonRef e) {
		if (getRef13ix() == null || (e.ref != 0 && e.ref != getRef13ix())) {
			return;
		}
		UIEventProcessor.uiAccess(this, this.uiEventBus, () -> {
			this.topWrapper.removeAll();
			this.topWrapper.add(this.juryRow);

			UI currentUI = UI.getCurrent();
			new DelayTimer().schedule(() -> currentUI.access(() -> {
				this.beeper.beep();
			}), 1000);
			new DelayTimer().schedule(() -> currentUI.access(() -> {
				this.beeper.reset();
				this.topWrapper.removeAll();
				this.topWrapper.add(this.topRow);
			}), 9000);
		});
	}

	@Subscribe
	public void slaveTimeStarted(UIEvent.StartTime e) {
		UIEventProcessor.uiAccess(this, this.uiEventBus, () -> {
			resetRefVote();
		});
	}

	@Subscribe
	public void slaveWakeUpRef(UIEvent.WakeUpRef e) {
		if (getRef13ix() == null || e.ref != getRef13ix()) {
			return;
		}
		UIEventProcessor.uiAccess(this, this.uiEventBus, () -> {
			if (e.on) {
				this.topWrapper.removeAll();
				this.topWrapper.add(this.warningRow);
				logger.debug("beeping");
				this.beeper.beep();
			}
		});
	}

	protected ComboBox<FieldOfPlay> createFopSelect() {
		ComboBox<FieldOfPlay> fopSelect = new ComboBox<>();
		fopSelect.setPlaceholder(Translator.translate("SelectPlatform"));
		fopSelect.setItems(OwlcmsFactory.getFOPs());
		fopSelect.setItemLabelGenerator(FieldOfPlay::getName);
		fopSelect.setWidth("10rem");
		return fopSelect;
	}

	protected void init() {
		this.setBoxSizing(BoxSizing.BORDER_BOX);
		this.setSizeFull();
		this.beeper = new BeepElement();
		createContent(this);
	}

	/**
	 * @see com.vaadin.flow.component.Component#onAttach(com.vaadin.flow.component.AttachEvent)
	 */
	@Override
	protected void onAttach(AttachEvent attachEvent) {
		// crude workaround -- randomly getting light or dark due to multiple themes
		// detected in app.
		getElement().executeJs("document.querySelector('html').setAttribute('theme', 'dark');");

		SoundUtils.enableAudioContextNotification(this.getElement());
		OwlcmsSession.withFop(fop -> {
			// we listen on uiEventBus.
			this.uiEventBus = uiEventBusRegister(this, fop);
		});
	}

	private Icon bigIcon(VaadinIcon iconDef, String color) {
		Icon icon = iconDef.create();
		icon.setSize("70%");
		icon.getStyle().set("color", color);
		return icon;
	}

	private void createContent(VerticalLayout refContainer) {
		var w = new Div();
		w.add(new Text(Translator.translate("JuryNotification.PleaseEnterDecision")));
		w.getStyle()
		        .set("background-color", "yellow")
		        .set("color", "black")
		        .set("text-align", "center")
		        .set("font-size", "larger")
		        .set("font-weight", "bold");
		w.getClassNames().add("blink");
		w.setWidth("100%");
		this.warningRow = new HorizontalLayout();
		this.warningRow.add(w);
		this.warningRow.setPadding(false);
		this.warningRow.setMargin(false);
		this.warningRow.setWidthFull();
		this.warningRow.getStyle().set("background-color", "yellow");

		var j = new H2(Translator.translate("JuryNotification.PleaseSeeJury"));
		j.getStyle()
		        .set("background-color", "red")
		        .set("color", "white")
		        .set("text-align", "center")
		        .set("font-size", "larger")
		        .set("font-weight", "bold");
		j.getClassNames().add("blink");
		j.setWidth("100%");
		this.juryRow = new HorizontalLayout();
		this.juryRow.add(j);
		this.juryRow.setWidthFull();
		this.juryRow.setPadding(false);
		this.juryRow.setMargin(false);
		this.juryRow.getStyle().set("background-color", "red");

		NativeLabel refLabel = new NativeLabel(Translator.translate("Referee"));
		var labelWrapper = new H2(refLabel);
		labelWrapper.getStyle().set("margin-top", "0");
		labelWrapper.getStyle().set("margin-bottom", "0");

		this.refField = new IntegerField();
		this.refField.setStep(1);
		this.refField.setMax(3);
		this.refField.setMin(1);
		this.refField.setValue(getRef13ix() == null ? null : getRef13ix().intValue());
		this.refField.setPlaceholder(Translator.translate("Number"));
		this.refField.setStepButtonsVisible(true);
		this.refField.addValueChangeListener((e) -> {
			setRef13ix(e.getValue());
			setUrl(getRef13ix() != null ? getRef13ix().toString() : null);
		});

		ComboBox<FieldOfPlay> fopSelect = createFopSelect();
		fopSelect.setValue(OwlcmsSession.getFop());
		fopSelect.addValueChangeListener((e) -> {
			OwlcmsSession.setFop(e.getValue());
		});

		this.topRow = new HorizontalLayout();
		this.topRow.add(labelWrapper, fopSelect, this.refField);
		this.topRow.setMargin(false);
		this.topRow.setSpacing(true);
		this.topRow.setAlignItems(Alignment.BASELINE);

		this.topWrapper = new HorizontalLayout();
		this.topWrapper.setHeight("2em");
		this.topWrapper.setWidthFull();
		this.topWrapper.getStyle().set("line-height", "2em");

		createRefVoting();
		resetRefVote();

		refContainer.setId("refContainer");
		refContainer.setBoxSizing(BoxSizing.BORDER_BOX);
		refContainer.setMargin(false);
		refContainer.getClassNames().add("dark");
		refContainer.setHeight("100%");
		refContainer.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		HorizontalLayout tl = new HorizontalLayout(this.beeper, this.topWrapper);
		tl.setWidthFull();
		refContainer.add(tl);
		refContainer.setAlignSelf(Alignment.START, tl);
		this.refVotingCenterHorizontally.setId("refVotingCenterHorizontally");
		this.setId("top");
		refContainer.add(this.refVotingCenterHorizontally);
	}

	private void createRefVoting() {
		// center buttons vertically, spread withing proper width
		this.refVotingButtons = new HorizontalLayout();
		this.refVotingButtons.setBoxSizing(BoxSizing.BORDER_BOX);
		this.refVotingButtons.setJustifyContentMode(JustifyContentMode.EVENLY);
		this.refVotingButtons.setDefaultVerticalComponentAlignment(Alignment.CENTER);
		this.refVotingButtons.setHeight("100%");
		this.refVotingButtons.setWidth("100%");
		this.refVotingButtons.getStyle().set("background-color", "black");
		this.refVotingButtons.setPadding(false);
		this.refVotingButtons.setMargin(false);
		this.refVotingButtons.setSpacing(true);

		// center the button cluster within page width
		this.refVotingCenterHorizontally = new VerticalLayout();
		this.refVotingCenterHorizontally.setSizeFull();
		this.refVotingCenterHorizontally.setBoxSizing(BoxSizing.BORDER_BOX);
		this.refVotingCenterHorizontally.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		this.refVotingCenterHorizontally.setPadding(true);
		this.refVotingCenterHorizontally.setMargin(true);

		this.refVotingCenterHorizontally.add(this.refVotingButtons);
	}

	private void doRed() {
		OwlcmsSession.withFop(fop -> {
			if (getRef13ix() == null) {
				return;
			}
			fop.fopEventPost(new FOPEvent.DecisionUpdate(getOrigin(), getRef13ix() - 1, false));
		});
		doRedColor();
	}

	private void doRedColor() {
		this.good.getStyle().set("color", "DarkSlateGrey");
		this.good.getStyle().set("outline-color", "white");
		this.bad.getStyle().set("color", "red");
		this.topWrapper.removeAll();
		this.topWrapper.add(this.topRow);
		this.beeper.reset();
	}

	private void doWhite() {
		OwlcmsSession.withFop(fop -> {
			if (getRef13ix() == null) {
				return;
			}
			fop.fopEventPost(new FOPEvent.DecisionUpdate(getOrigin(), getRef13ix() - 1, true));
		});
		doWhiteColor();
	}

	private void doWhiteColor() {
		this.bad.getStyle().set("color", "DarkSlateGrey");
		this.bad.getStyle().set("outline-color", "white");
		this.good.getStyle().set("color", "white");
		this.topWrapper.removeAll();
		this.topWrapper.add(this.topRow);
		this.beeper.reset();
	}

	private Object getOrigin() {
		return this;
	}

	private Integer getRef13ix() {
		return this.ref13ix;
	}

	private void redClicked(DomEvent e) {
		if (!this.redTouched) {
			doRed();
		}
	}

	private void redTouched(DomEvent e) {
		this.redTouched = true;
		doRed();
		vibrate();
	}

	private void resetRefVote() {
		this.refVotingButtons.removeAll();
		this.good = bigIcon(VaadinIcon.CHECK_CIRCLE, "white");
		this.good.getElement().addEventListener("touchstart", (e) -> whiteTouched(e));
		this.good.getElement().addEventListener("click", (e) -> whiteClicked(e));
		this.bad = bigIcon(VaadinIcon.CLOSE_CIRCLE, "red");
		this.bad.getElement().addEventListener("touchstart", (e) -> redTouched(e));
		this.bad.getElement().addEventListener("click", (e) -> redClicked(e));
		this.refVotingButtons.add(this.bad, this.good);
		this.topWrapper.removeAll();
		this.topWrapper.add(this.topRow);
		this.beeper.reset();
	}

	private void setRef13ix(Integer ref13ix) {
		this.ref13ix = ref13ix;
	}

	private void setUrl(String num) {
		if (num != null) {
			this.urlParams.put(REF_INDEX, Arrays.asList(num));
		} else {
			this.urlParams.remove(REF_INDEX);
		}
		// change the URL to reflect group
		Location location2 = new Location(this.location.getPath(), new QueryParameters(this.urlParams));
		this.locationUI.getPage().getHistory().replaceState(null, location2);
		logger.trace("changed location to {}", location2.getPathWithQueryParameters());
		UI.getCurrent().getPage().setTitle(getPageTitle());
	}

	private void vibrate() {
		UI.getCurrent().getPage().executeJs("window.navigator.vibrate", 200);
	}

	private void whiteClicked(DomEvent e) {
		if (!this.whiteTouched) {
			doWhite();
		}
		vibrate();
	}

	private void whiteTouched(DomEvent e) {
		this.whiteTouched = true;
		doWhite();
	}

}
