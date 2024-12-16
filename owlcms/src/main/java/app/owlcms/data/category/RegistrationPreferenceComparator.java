/*******************************************************************************
 * Copyright (c) 2009-2023 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("NPOSL-3.0")
 * License text at https://opensource.org/licenses/NPOSL-3.0
 *******************************************************************************/
package app.owlcms.data.category;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;

import app.owlcms.data.agegroup.AgeGroup;
import app.owlcms.data.agegroup.Championship;
import app.owlcms.data.config.Config;

/**
 * When several categories are possible for an athlete, this class returns the preferred choice.
 *
 * Given a 36 year old athlete, they could be
 * <ul>
 * <li>M35 (35-39)
 * <li>O21 (21+) or
 * <li>SR (15+)
 * </ul>
 * in that order of preference if all 3 categories are active. The athlete would be placed as a M35 by default, and the choice can be overriden.
 *
 * Given a 15 year old athlete they could be
 * <ul>
 * <li>U15 (13-15)
 * <li>JR (15-20) or
 * <li>SR (15+)
 * </ul>
 * in that order of preference. Normally youth age groups would not be used in addition to JR because of the ambiguity, but JR and SR could be used at the same
 * time. The lifter would be shown on the boards as JR.
 *
 */
public class RegistrationPreferenceComparator implements Comparator<Category> {

	@Override
	public int compare(Category c1, Category c2) {
		// null is larger -- will show at the end
		if (c1 == null && c2 == null) {
			return 0;
		} else if (c1 == null && c2 != null) {
			return 1;
		} else if (c1 != null && c2 == null) {
			return -1;
		} else if (c1 != null && c2 != null) {
			if (!Config.getCurrent().featureSwitch("oldCatOrder")) {
				return ObjectUtils.compare(c1.getMedalingSortCode(), c2.getMedalingSortCode());
			} else {
				AgeGroup ag1 = c1.getAgeGroup();
				AgeGroup ag2 = c2.getAgeGroup();
				Championship ad1 = (ag1 != null ? ag1.getChampionship() : null);
				Championship ad2 = (ag2 != null ? ag2.getChampionship() : null);

				int compare = 0;
				if (ad1 != null && ad2 != null && ag1 != null && ag2 != null) {
					compare = ObjectUtils.compare(c1.getGender(), c2.getGender());
					if (compare != 0) {
						return compare;
					}

					// Championships are in registration preference order
					compare = ObjectUtils.compare(ad1.getType(), ad2.getType());
					if (compare != 0) {
						return compare;
					}

					// Championships are in registration preference order
					compare = ObjectUtils.compare(ad1.getName().length(), ad2.getName().length());
					if (compare != 0) {
						return compare;
					}

					// athlete will be placed in youngest age group by default
					compare = Integer.compare(ag1.getMinAge(), ag2.getMinAge());
					if (compare != 0) {
						return compare;
					}

					// same minimum age, listed in most specific age category
					compare = ObjectUtils.compare(ag1.getMaxAge(), ag2.getMaxAge());
					if (compare != 0) {
						return compare;
					}

					// compare age divisions -- grasping at straws to get a total order.
					compare = ObjectUtils.compare(ad1, ad2);
					if (compare != 0) {
						return compare;
					}
				}

				// compare max body weights
				compare = Double.compare(c1.getMaximumWeight(), c2.getMaximumWeight());
				if (compare != 0) {
					return compare;
				}

				return 0;
			}
		} else {
			throw new RuntimeException("can't happen");
		}
	}

}
