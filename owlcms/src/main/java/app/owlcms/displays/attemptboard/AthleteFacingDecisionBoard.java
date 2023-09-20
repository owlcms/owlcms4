/*******************************************************************************
 * Copyright (c) 2009-2023 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("NPOSL-3.0")
 * License text at https://opensource.org/licenses/NPOSL-3.0
 *******************************************************************************/
package app.owlcms.displays.attemptboard;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.router.Route;

import app.owlcms.data.competition.Competition;
import app.owlcms.fieldofplay.FOPState;
import app.owlcms.fieldofplay.FieldOfPlay;
import app.owlcms.init.OwlcmsSession;

@SuppressWarnings("serial")
@Tag("decision-board-template")
@JsModule("./components/DecisionBoard.js")
@JsModule("./components/AudioContext.js")
@Route("displays/athleteFacingDecision")

public class AthleteFacingDecisionBoard extends AttemptBoard {

	public AthleteFacingDecisionBoard() {
		super();
		setPublicFacing(false);
		setShowBarbell(false);
		breakTimer.setParent("DecisionBoard");
	}

	@Override
	public String getPageTitle() {
		return getTranslation("Decision_AF_") + OwlcmsSession.getFopNameIfMultiple();
	}

	@Override
	public boolean isPublicFacing() {
		return isPublicFacing();
	}

	@Override
	public boolean isSilencedByDefault() {
		return false;
	}

	@Override
	protected void checkImages() {
		athletePictures = false;
		teamFlags = false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see app.owlcms.displays.attemptboard.AttemptBoard#onAttach(com.vaadin.flow.
	 * component.AttachEvent)
	 */
	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		decisions.setPublicFacing(false);
	}
	
	protected void doEmpty() {
		FieldOfPlay fop2 = OwlcmsSession.getFop();
		boolean inactive = fop2 == null || fop2.getState() == FOPState.INACTIVE;
		this.getElement().callJsFunction("clear");
		this.getElement().setProperty("inactiveBlockStyle", (inactive ? "display:grid" : "display:none"));
		this.getElement().setProperty("activeGridStyle", (inactive ? "display:none" : "display:grid"));
		this.getElement().setProperty("inactiveClass", (inactive ? "bigTitle" : ""));
		this.getElement().setProperty("competitionName", Competition.getCurrent().getCompetitionName());
	}
}