/*******************************************************************************
 * Copyright (c) 2009-2023 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("NPOSL-3.0")
 * License text at https://opensource.org/licenses/NPOSL-3.0
 *******************************************************************************/
package app.owlcms.data.scoring;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.Nullable;

import org.slf4j.LoggerFactory;

import app.owlcms.data.athlete.Athlete;
import app.owlcms.data.athlete.Gender;
import app.owlcms.utils.LoggerUtils;
import app.owlcms.utils.ResourceWalker;
import ch.qos.logback.classic.Logger;

/**
 *
 * Compute Q Points according to https://osf.io/8x3nb/ (formulas in https://osf.io/download/r2gxa/ and https://osf.io/download/bmctw/)
 *
 * The age coefficients are the 2025 coefficients for QMasters
 */
public class QPoints {

	Logger logger = (Logger) LoggerFactory.getLogger(QPoints.class);
	Properties props = null;
	private HashMap<Integer, Float> men = null;
	private HashMap<Integer, Float> women = null;
	private int qpointsYear;
	private Double menTMax;
	private Double menBeta0;
	private Double menBeta1;
	private Double menBeta2;
	private Double womenTMax;
	private Double womenBeta0;
	private Double womenBeta1;
	private Double womenBeta2;

	public QPoints(int i) {
		this.qpointsYear = 2025;
		// don't load coefficients -- Athlete calls us too early and config not loaded.
	}

	/**
	 * @param age
	 * @return the Age/Gender Coefficient for that age.
	 * @throws IOException
	 */
	public Float getAgeGenderCoefficient(@Nullable Integer age, @Nullable Gender gender) {
		if ((gender == null) || (age == null)) {
			return 0.0F;
		}
		switch (gender) {
			case M:
				if (this.men == null) {
					loadMap();
				}
				if (age <= 30) {
					return 1.0F;
				}
				if (age >= 95) {
					return this.men.get(95);
				}
				return this.men.get(age);
			case F:
				if (this.women == null) {
					loadMap();
				}
				if (age <= 30) {
					return 1.0F;
				}
				if (age >= 90) {
					return this.women.get(90);
				}
				return this.women.get(age);
			case I:
				return 1.0F;
			default:
				break;
		}
		return 0.0F;
	}

	public Double getMenBeta0() {
		loadCoefficients();
		return this.menBeta0;
	}

	public Double getMenBeta1() {
		loadCoefficients();
		return this.menBeta1;
	}

	public Double getMenBeta2() {
		loadCoefficients();
		return this.menBeta2;
	}

	public Double getMenTMax() {
		loadCoefficients();
		return this.menTMax;
	}

	public Double getQPoints(Athlete a, Integer value) {
		if (value == null) {
			return 0.0D;
		}
		Gender gender = a.getGender();
		if (gender == null) {
			return 0.0D;
		}
		Double bw = a.getBodyWeight();
		if (bw == null) {
			return 0.0D;
		}
		Double qPointsFactor = qPointsFactor(gender, bw);
		return value * qPointsFactor;
	}

	public Double getWomenBeta0() {
		loadCoefficients();
		return this.womenBeta0;
	}

	public Double getWomenBeta1() {
		loadCoefficients();
		return this.womenBeta1;
	}

	public Double getWomenBeta2() {
		loadCoefficients();
		return this.womenBeta2;
	}

	public Double getWomenTMax() {
		loadCoefficients();
		return this.womenTMax;
	}

	public Double qPointsFactor(Gender gender, Double bw) {
		double qPointsFactor = 0D;
		try {
			Double beta0;
			Double beta1;
			Double beta2;
			Double tMax;
			switch (gender) {
				case F:
					beta0 = getWomenBeta0();
					beta1 = getWomenBeta1();
					beta2 = getWomenBeta2();
					tMax = getWomenTMax();
					break;
				case M:
					beta0 = getMenBeta0();
					beta1 = getMenBeta1();
					beta2 = getMenBeta2();
					tMax = getMenTMax();
					break;
				default:
					return 0.0D;
			}
			qPointsFactor = (tMax / (beta0 - beta1 * Math.pow((bw / 100.0D), -2) + beta2 * Math.pow((bw / 100.0D), 2)));
		} catch (Exception e) {
			LoggerUtils.logError(this.logger, e);
		}
		return qPointsFactor;
	}

	private void loadCoefficients() {
		if (this.menTMax != null) {
			return;
		}
		if (this.props == null) {
			loadProps();
		}
		this.setMenTMax(Double.valueOf((String) this.props.get("qpoints.menTMax")));
		this.setMenBeta0(Double.valueOf((String) this.props.get("qpoints.menBeta0")));
		this.setMenBeta1(Double.valueOf((String) this.props.get("qpoints.menBeta1")));
		this.setMenBeta2(Double.valueOf((String) this.props.get("qpoints.menBeta2")));
		this.setWomenTMax(Double.valueOf((String) this.props.get("qpoints.womenTMax")));
		this.setWomenBeta0(Double.valueOf((String) this.props.get("qpoints.womenBeta0")));
		this.setWomenBeta1(Double.valueOf((String) this.props.get("qpoints.womenBeta1")));
		this.setWomenBeta2(Double.valueOf((String) this.props.get("qpoints.womenBeta2")));
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private HashMap<Integer, Float> loadMap() {

		if (this.props == null) {
			loadProps();
		}

		this.men = new HashMap<>((this.props.size()));
		this.women = new HashMap<>((this.props.size()));

		for (Entry<Object, Object> entry : this.props.entrySet()) {
			String curKey = (String) entry.getKey();
			if (curKey.startsWith("men.")) {
				this.men.put(Integer.valueOf(curKey.replace("men.", "")), Float.valueOf((String) entry.getValue()));
			} else if (curKey.startsWith("women.")) {
				this.women.put(Integer.valueOf(curKey.replace("women.", "")), Float.valueOf((String) entry.getValue()));
			}
		}
		return this.men;
	}

	/**
	 * @throws IOException
	 */
	private void loadProps() {
		this.props = new Properties();
		String name = "/qpoints/qpoints" + this.qpointsYear + ".properties";
		try {
			InputStream stream = ResourceWalker.getResourceAsStream(name);
			this.props.load(stream);
		} catch (Exception e) {
			this.logger.error("could not load {} because {}\n{}", name, e, LoggerUtils.stackTrace(e));
		}
	}

	private void setMenBeta0(Double menBeta0) {
		this.menBeta0 = menBeta0;
	}

	private void setMenBeta1(Double menBeta1) {
		this.menBeta1 = menBeta1;
	}

	private void setMenBeta2(Double menBeta2) {
		this.menBeta2 = menBeta2;
	}

	private void setMenTMax(Double menTMax) {
		this.menTMax = menTMax;
	}

	private void setWomenBeta0(Double womenBeta0) {
		this.womenBeta0 = womenBeta0;
	}

	private void setWomenBeta1(Double womenBeta1) {
		this.womenBeta1 = womenBeta1;
	}

	private void setWomenBeta2(Double womenBeta2) {
		this.womenBeta2 = womenBeta2;
	}

	private void setWomenTMax(Double womenTMax) {
		this.womenTMax = womenTMax;
	}
}
