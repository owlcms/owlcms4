package app.owlcms.data.athleteSort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;

import app.owlcms.data.athlete.Athlete;
import app.owlcms.i18n.Translator;
import ch.qos.logback.classic.Logger;

/**
 * The Enum Ranking.
 */
public enum Ranking {
    // category values
	SNATCH("Sn",false),
	CLEANJERK("CJ",false),
	TOTAL("Tot",false),
	CUSTOM("Cus",false), // modified total / custom score (e.g. technical merit for kids competition)
	SNATCH_CJ_TOTAL("Combined",false), // sum of all three point scores
	CATEGORY_SCORE("SCORE",false), // copy of TOTAL, CUSTOM or any of the global scoring systems if used to award category medals

    // global scoring systems
	BW_SINCLAIR("Sinclair",true), // normal Sinclair
	CAT_SINCLAIR("CatSinclair",true), // legacy Quebec federation, Sinclair computed at category boundary
	SMM("Smm",true), // Legacy name, kept for import/export backward compatibility Sinclair Meltzer Huebner Faber
	ROBI("Robi",true), // IWF ROBI
	QPOINTS("QPoints",true), // Huebner QPoints.
	GAMX("GAMX",true), // Global Adjusted Mixed (Huebner)
	AGEFACTORS("QYouth",true),
	QAGE("QAge",true), // QPoints * SMHF age factors
	;

	static Logger logger = (Logger) LoggerFactory.getLogger(Ranking.class);

	public static int getRanking(Athlete curLifter, Ranking rankingType) {
		Integer value = null;
		if (rankingType == null) {
			return 0;
		}
		switch (rankingType) {
			case SNATCH:
				value = curLifter.getSnatchRank();
				break;
			case CLEANJERK:
				value = curLifter.getCleanJerkRank();
				break;
			case TOTAL:
				value = curLifter.getTotalRank();
				break;
			case ROBI:
				value = curLifter.getRobiRank();
				break;
			case CUSTOM:
				value = curLifter.getCustomRank();
				break;
			case SNATCH_CJ_TOTAL:
				value = 0; // no such thing
				break;
			case BW_SINCLAIR:
				value = curLifter.getSinclairRank();
				break;
			case CAT_SINCLAIR:
				value = curLifter.getCatSinclairRank();
				break;
			case SMM:
				value = curLifter.getSmhfRank();
				break;
			case GAMX:
				value = curLifter.getGamxRank();
				break;
			case QPOINTS:
				value = curLifter.getqPointsRank();
				break;
			case QAGE:
				value = curLifter.getqAgeRank();
				break;
			case AGEFACTORS:
				value = curLifter.getAgeAdjustedTotalRank();
				break;
			case CATEGORY_SCORE:
				value = curLifter.getCategoryScoreRank();
				break;
		}
		// logger.debug("{} ranking value: {}", curLifter.getShortName(), value);
		return value == null ? 0 : value;
	}

	/**
	 * @param curLifter
	 * @param rankingType
	 * @return
	 */
	public static double getRankingValue(Athlete curLifter, Ranking rankingType) {
		if (rankingType == null) {
			return 0D;
		}
		Double d = 0D;
		Integer i = 0;
		switch (rankingType) {
			case SNATCH:
				i = curLifter.getBestSnatch();
				d = i != null ? i.doubleValue() : null;
				break;
			case CLEANJERK:
				i = curLifter.getBestCleanJerk();
				d = i != null ? i.doubleValue() : null;
				break;
			case TOTAL:
				i = curLifter.getTotal();
				d = i != null ? i.doubleValue() : null;
				break;
			case ROBI:
				d = curLifter.getRobi();
				break;
			case CUSTOM:
				d = curLifter.getCustomScore();
				break;
			case SNATCH_CJ_TOTAL:
				d = 0D; // no such thing
				break;
			case BW_SINCLAIR:
				d = curLifter.getSinclair();
				break;
			case CAT_SINCLAIR:
				d = curLifter.getCategorySinclair();
				break;
			case SMM:
				d = curLifter.getSmhfForDelta();
				break;
			case GAMX:
				d = curLifter.getGamx();
				break;
			case AGEFACTORS:
				d = curLifter.getAgeAdjustedTotal();
				break;
			case QPOINTS:
				d = curLifter.getQPoints();
				break;
			case QAGE:
				d = curLifter.getQAge();
				break;
			case CATEGORY_SCORE:
				d = curLifter.getCategoryScore();
				break;
		}
		return d != null ? d : 0D;
	}

	public static String getScoringTitle(Ranking rankingType) {
		if (rankingType == null || rankingType == Ranking.CATEGORY_SCORE) {
			return Translator.translate("Score");
		}
		switch (rankingType) {
			case ROBI:
			case CUSTOM:
			case BW_SINCLAIR:
			case CAT_SINCLAIR:
			case SMM:
			case GAMX:
			case QPOINTS:
			case AGEFACTORS:
			case QAGE:
			case TOTAL:
				return Translator.translate("Ranking." + rankingType);
			default:
				throw new UnsupportedOperationException("not a score ranking " + rankingType);
		}
	}

	public static String getScoringExplanation(Ranking rankingType) {
		if (rankingType == null || rankingType == Ranking.CATEGORY_SCORE) {
			return Translator.translate("Score");
		}
		switch (rankingType) {
			case ROBI:
			case CUSTOM:
			case BW_SINCLAIR:
			case CAT_SINCLAIR:
			case SMM:
			case GAMX:
			case QPOINTS:
			case AGEFACTORS:
			case QAGE:
			case TOTAL:
				return Translator.translate("RankingExplanation." + rankingType);
			default:
				throw new UnsupportedOperationException("not a score ranking " + rankingType);
		}
	}

	public static List<Ranking> scoringSystems() {
		List<Ranking> systems = new ArrayList<>(Arrays.asList(BW_SINCLAIR, SMM, ROBI, AGEFACTORS, QPOINTS, QAGE, GAMX, CAT_SINCLAIR));
		return systems;
	}

	private String reportingName;
	private boolean medalScore;

	/**
	 * @param medalScore 
	 * @param reportingInfoName the name of the beans used for Excel reporting
	 */
	Ranking(String reportingName, boolean medalScore) {
		this.reportingName = reportingName;
		this.medalScore = medalScore;
	}

	public String getMReportingName() {
		return "m" + this.reportingName;
	}

	public String getMWReportingName() {
		return "mw" + this.reportingName;
	}

	public String getWReportingName() {
		return "w" + this.reportingName;
	}

	public static String formatScoreboardRank(Integer total) {
		if (total == null || total == 0) {
			return "-";
		} else if (total == -1) {
			// invited lifter, not eligible.
			return Translator.translate("Results.Extra/Invited");
		} else {
			return total.toString();
		}
	}

	public boolean isMedalScore() {
		return medalScore;
	}

	public void setMedalScore(boolean medalScore) {
		this.medalScore = medalScore;
	}

}