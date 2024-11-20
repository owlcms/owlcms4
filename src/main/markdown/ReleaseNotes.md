> [!WARNING]
>
> - This is a **beta release**, used for testing and translation. ***Some features could be non-functional***.
> - Beta releases are **not** normally used in actual competitions, except when a new feature is required. Use extreme care in testing if you intend to do so.

- Maintenance log:
  - 54.0.0-beta07: improved robustness of the verification that a session is done.
  - 54.0.0-beta06: Changing the championship for an age group was not doing anything. Fixed.
  - 54.0.0-beta06: On the Age Group definition page, added a button to rename/add/remove championships.
  - 54.0.0-beta06: It is now possible to select a scoring system for assigning medals on the age group editing form.  By default, no scoring system is selected, meaning that the traditional way  using the total and optionally the snatch/clean-and-jerk is used)
  - 54.0.0-beta05: Small fixes to templates. 
  - 54.0.0-beta04: Updated the competition results and the protocol sheets to use the faster jxls3 template processing. The categories are now listed in alphabetical order.
  - 54.0.0-beta03: On Raspberry Pi, the server would not start until the initial browser window was killed. The browser window was waiting for the server (deadlock)
  - 54.0.0-beta03: Removed obsolete attempt at translating the default championship types in the dropdowns
  - 54.0.0-beta03: Fixed missing translation for the "load results from a session that took place in another building" feature.
  - 54.0.0-beta03: Further adjustments to medals displays.
  - 54.0.0-beta03: Fixed colors on the light-themed medals scoreboards, and adjusted the columns on the video "green transparency" medal display.
  - 54.0.0-beta03: Athlete cards were missing the session information on the top line.
  - 54.0.0-beta02: Changing the medals category shown on the video-specific medals page no longer interferes with the normal medals page and the public main room scoreboard (if the "public" main room scoreboard, the medals page is shown during medal ceremonies)
  - 54.0.0-beta01: The medals scoreboard now correctly reacts to group switches. Also reacts correctly to medal category switches during medal ceremonies and end of medal ceremonies.
  - 54.0.0-beta01: The public scoreboard was not switching to medals during ceremonies, now fixed.
  - 54.0.0-alpha03: the rank recalculation on the competition results page was not recomputing all categories, now fixed.
  - 54.0.0-alpha02: reworked the category ranking code for performance when athletes are registered in many eligibility categories.
  - 54.0.0-alpha01: initial release of scoreboard cleanup
- Scoreboards now correctly display ranks and leaders for categories where medals are given based on a score (see documentation for [score-based medals](https://jflamy.github.io/owlcms4/#/ScoreBasedCompetitions))
- Medals scoreboards and medals reports have now been fixed to handle score-based medals
- Results
  - During a competition with both score-based and total-based rankings, from the Competition Results page, using the Eligibility Categories report with the Score template will produce correct interim or final results.   Each category will be ranked according to it's scoring system.
  - Updated the competition results and the protocol sheets to use the faster jxls3 template processing. The categories are now listed in alphabetical order.
- Templates:
  - the athlete's score and ranks in the current category are now obtained by using `${l.categoryScore}` `${l.categoryScoreRank}` (where l is the loop variable giving the current athlete).  
  - If the current category is not score-based, this is the same as `${l.total}`and the `${l.totalRank}`, so it is always possible to use the `Score` templates for a total-based competition.
- Bar Loading:
  - The weight under which collars are not used is now configurable.  Default is 40kg.
  - The normal grey bar color is used when 15kg bar is used for women or 20kg is used for men, even if the non-standard bar or children loading rules are in effect.

For other recent changes, see [version 52 release notes](https://github.com/owlcms/owlcms4/releases/tag/52.0.6) and [version 53 release notes](https://github.com/owlcms/owlcms4/releases/tag/53.1.0)
