/*******************************************************************************
 * Copyright (c) 2009-2023 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("NPOSL-3.0")
 * License text at https://opensource.org/licenses/NPOSL-3.0
 *******************************************************************************/
package app.owlcms.nui.home;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

import app.owlcms.apputils.AccessUtils;
import app.owlcms.i18n.Translator;
import app.owlcms.init.OwlcmsSession;
import app.owlcms.nui.shared.ContentWrapping;
import app.owlcms.nui.shared.OwlcmsLayout;
import app.owlcms.nui.shared.OwlcmsLayoutAware;
import app.owlcms.nui.shared.RequireLogin;
import ch.qos.logback.classic.Logger;

/**
 * Check for proper credentials.
 *
 * Scenarios:
 * <ul>
 * <li>If the IP environment variable is present, it is expected to be a commma-separated address list of IPv4
 * addresses. Browser must come from one of these addresses The IP address(es) will normally be those for the local
 * router or routers used at the competition site.
 * <li>if a PIN environment variable is present, the PIN will be required (even if no IP whitelist)
 * <li>if PIN enviroment variable is not present, all accesses from the whitelisted routers will be allowed. This can be
 * sufficient if the router password is well-protected (which is not likely). Users can type any NIP, including an empty
 * value.
 * <li>if neither IP nor PIN is present, no check is done ({@link RequireLogin} does not display this view).
 * </ul>
 */
@SuppressWarnings("serial")
@Route(value = LoginView.LOGIN, layout = OwlcmsLayout.class)
public class LoginView extends Composite<VerticalLayout>
        implements OwlcmsLayoutAware, ContentWrapping, HasDynamicTitle {

	public static final String LOGIN = "login";
	static Logger logger = (Logger) LoggerFactory.getLogger(LoginView.class);
	private PasswordField pinField = new PasswordField();
	private OwlcmsLayout routerLayout;

	public LoginView() {
		this.pinField.setClearButtonVisible(true);
		this.pinField.setRevealButtonVisible(true);
		this.pinField.setLabel(Translator.translate("EnterPin"));
		this.pinField.setWidthFull();
		this.pinField.addValueChangeListener(event -> {
			String value = event.getValue();
			logger.debug("login input {}", value);
			if (checkAuthenticated(value)) {
				this.pinField.setErrorMessage(Translator.translate("LoginDenied"));
				this.pinField.setInvalid(true);
			} else {
				this.pinField.setInvalid(false);
				redirect();
			}
		});

		// brute-force the color because some display views use a white text color.
		H3 h3 = new H3(Translator.translate("Log_In"));
		h3.getStyle().set("color", "var(--lumo-header-text-color)");
		h3.getStyle().set("font-size", "var(--lumo-font-size-xl)");

		Button button = new Button(Translator.translate("Login"));
		button.addClickShortcut(Key.ENTER);
		button.setWidth("10em");
		button.getThemeNames().add("primary");
		button.getThemeNames().add("icon");

		VerticalLayout form = new VerticalLayout();
		form.add(h3, this.pinField, button);
		form.setWidth("20em");
		form.setAlignSelf(Alignment.CENTER, button);

		getContent().add(form);

	}

	@Override
	public FlexLayout createMenuArea() {
		return new FlexLayout();
	}

	public String getMenuTitle() {
		return Translator.translate("OWLCMS_Top");
	}

	@Override
	public String getPageTitle() {
		return Translator.translate("Login");
	}

	@Override
	public OwlcmsLayout getRouterLayout() {
		return this.routerLayout;
	}

	@Override
	public void setHeaderContent() {
		NativeLabel label = new NativeLabel(getMenuTitle());
		label.getStyle().set("font-size", "var(--lumo-font-size-xl");
		Image image = new Image("icons/owlcms.png", "owlcms icon");
		image.getStyle().set("height", "7ex");
		image.getStyle().set("width", "auto");
		HorizontalLayout topBarTitle = new HorizontalLayout(image, label);
		topBarTitle.setAlignSelf(Alignment.CENTER, label);
		this.routerLayout.setMenuTitle(topBarTitle);
		this.routerLayout.setMenuArea(createMenuArea());
		this.routerLayout.showLocaleDropdown(true);
		this.routerLayout.setDrawerOpened(true);
		this.routerLayout.updateHeader(true);
	}

	@Override
	public void setPadding(boolean b) {
		// not needed
	}

	@Override
	public void setRouterLayout(OwlcmsLayout routerLayout) {
		this.routerLayout = routerLayout;
	}

	protected boolean checkAuthenticated(String value) {
		return !AccessUtils.checkAuthenticated(value);
	}

	protected void redirect() {
		String requestedUrl = OwlcmsSession.getRequestedUrl();
		if (requestedUrl != null) {
			UI.getCurrent().navigate(requestedUrl, OwlcmsSession.getRequestedQueryParameters());
		} else {
			UI.getCurrent().navigate(HomeNavigationContent.class);
		}
	}

}