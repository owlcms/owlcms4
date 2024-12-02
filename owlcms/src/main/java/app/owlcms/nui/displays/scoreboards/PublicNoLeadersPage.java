package app.owlcms.nui.displays.scoreboards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;

import app.owlcms.apputils.queryparameters.DisplayParameters;
import app.owlcms.apputils.queryparameters.SoundParameters;
import app.owlcms.data.config.Config;
import app.owlcms.displays.scoreboard.Results;
import app.owlcms.displays.scoreboard.ResultsMedals;
import app.owlcms.i18n.Translator;
import app.owlcms.init.OwlcmsSession;
import app.owlcms.uievents.CeremonyType;
import app.owlcms.uievents.UIEvent;
import ch.qos.logback.classic.Logger;

@SuppressWarnings("serial")
@Route("displays/publicSimple")

public class PublicNoLeadersPage extends PublicScoreboardPage {

	Logger logger;
	Logger uiEventLogger;
	Map<String, List<String>> urlParameterMap = new HashMap<>();
	private Results resultsBoard;
	protected UI ui;
	private ResultsMedals medalsBoard;

	public PublicNoLeadersPage() {
		// intentionally empty. superclass will call init() as required.
	}

	@Override
	public String getPageTitle() {
		return Translator.translate("Scoreboard") + OwlcmsSession.getFopNameIfMultiple();
	}


	@Override
	protected void setDefaultParameters() {
		// when navigating to the page, Vaadin will call setParameter+readParameters
		// these parameters will be applied.
		var initialMap = Map.of(
		        SoundParameters.SILENT, "true",
		        SoundParameters.DOWNSILENT, "true",
		        DisplayParameters.DARK, "true",
		        DisplayParameters.LEADERS, "false",
		        DisplayParameters.RECORDS, "false",
		        DisplayParameters.VIDEO, "false",
		        DisplayParameters.PUBLIC, "false",
		        SoundParameters.SINGLEREF, "false",
		        DisplayParameters.ABBREVIATED, Boolean.toString(Config.getCurrent().featureSwitch("shortScoreboardNames")));
		var additionalMap = Map.of(
		        SoundParameters.LIVE_LIGHTS, Boolean.toString(!Config.getCurrent().featureSwitch("noLiveLights")),
		        SoundParameters.SHOW_DECLARATIONS, "false",
		        SoundParameters.CENTER_NOTIFICATIONS, Boolean.toString(Config.getCurrent().featureSwitch("centerAnnouncerNotifications")),
		        SoundParameters.START_ORDER, "false");
		Map<String, String> fullMap = new TreeMap<>();
		fullMap.putAll(initialMap);
		fullMap.putAll(additionalMap);
		setDefaultParameters(QueryParameters.simple(fullMap));
	}
}
