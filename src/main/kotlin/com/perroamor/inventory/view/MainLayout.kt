package com.perroamor.inventory.view

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Header
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.theme.lumo.LumoUtility
import com.perroamor.inventory.security.SecurityService
import jakarta.annotation.security.PermitAll
import org.springframework.beans.factory.annotation.Autowired

@PageTitle("Perro Amor - Sistema de Inventario")
@PermitAll
class MainLayout(@Autowired private val securityService: SecurityService) : AppLayout() {

    private lateinit var viewTitle: H1

    init {
        setPrimarySection(Section.DRAWER)
        addDrawerContent()
        addHeaderContent()
    }

    private fun addHeaderContent() {
        val toggle = DrawerToggle()
        toggle.setAriaLabel("Menu toggle")

        viewTitle = H1()
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE)

        val header = Header()
        header.addClassNames(
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Display.FLEX,
            LumoUtility.Width.FULL
        )
        header.style.set("padding", "0 var(--lumo-space-m)")
        
        val leftSection = HorizontalLayout(toggle)
        leftSection.addClassNames(
            LumoUtility.AlignItems.CENTER,
            LumoUtility.JustifyContent.START
        )
        leftSection.setFlexGrow(0.0)
        leftSection.width = "auto"
        
        val centerSection = HorizontalLayout(viewTitle)
        centerSection.addClassNames(
            LumoUtility.AlignItems.CENTER,
            LumoUtility.JustifyContent.CENTER
        )
        centerSection.setFlexGrow(1.0)
        
        header.add(leftSection, centerSection)

        addToNavbar(false, header)
    }


    private fun addDrawerContent() {
        val appName = Span("🐕 Perro Amor")
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE)

        val header = Header(appName)
        header.addClassNames(
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Display.FLEX
        )
        header.style.set("padding", "var(--lumo-space-m)")

        // Información del usuario al inicio del menú
        val userInfo = createUserInfoSection()

        val scroller = Scroller(createNavigation())

        // Botón de cerrar sesión al final del menú
        val logoutSection = createLogoutSection()

        addToDrawer(header, userInfo, scroller, logoutSection)
    }

    private fun createUserInfoSection(): VerticalLayout {
        val userInfoLayout = VerticalLayout()
        userInfoLayout.isSpacing = false
        userInfoLayout.isPadding = false
        userInfoLayout.style.set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
        userInfoLayout.style.set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
        userInfoLayout.style.set("margin-bottom", "var(--lumo-space-s)")

        if (securityService.isAuthenticated()) {
            val currentUser = securityService.getAuthenticatedUser()
            val userName = Span("👤 ${currentUser?.fullName ?: securityService.getCurrentUsername() ?: "Usuario"}")
            userName.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.MEDIUM)
            userName.style.set("color", "var(--lumo-primary-text-color)")
            
            val userRole = if (securityService.isAdmin()) "Administrador" else "Usuario"
            val roleLabel = Span(userRole)
            roleLabel.addClassNames(LumoUtility.FontSize.XSMALL)
            roleLabel.style.set("color", "var(--lumo-secondary-text-color)")
            
            userInfoLayout.add(userName, roleLabel)
        }

        return userInfoLayout
    }

    private fun createLogoutSection(): VerticalLayout {
        val logoutLayout = VerticalLayout()
        logoutLayout.isSpacing = false
        logoutLayout.isPadding = false
        logoutLayout.style.set("padding", "var(--lumo-space-m)")
        logoutLayout.style.set("border-top", "1px solid var(--lumo-contrast-10pct)")

        if (securityService.isAuthenticated()) {
            val logoutButton = Button("Cerrar Sesión", Icon(VaadinIcon.SIGN_OUT)) {
                securityService.logout()
            }
            logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL)
            logoutButton.style.set("width", "100%")
            logoutButton.style.set("justify-content", "flex-start")
            
            logoutLayout.add(logoutButton)
        }

        return logoutLayout
    }

    private fun createNavigation(): SideNav {
        val nav = SideNav()

        nav.addItem(
            SideNavItem(
                "Inventario",
                ProductView::class.java,
                Icon(VaadinIcon.PACKAGE)
            )
        )

        nav.addItem(
            SideNavItem(
                "Variantes",
                "variants",
                Icon(VaadinIcon.SPLIT)
            )
        )

        nav.addItem(
            SideNavItem(
                "Catálogos",
                "catalogs",
                Icon(VaadinIcon.FOLDER)
            )
        )

        nav.addItem(
            SideNavItem(
                "Eventos",
                "events",
                Icon(VaadinIcon.CALENDAR)
            )
        )

        nav.addItem(
            SideNavItem(
                "Nueva Venta",
                "new-sale",
                Icon(VaadinIcon.PLUS_CIRCLE)
            )
        )

        nav.addItem(
            SideNavItem(
                "Ventas",
                "sales",
                Icon(VaadinIcon.CART)
            )
        )

        // Gestión de Usuarios (solo para ADMIN)
        if (securityService.isAdmin()) {
            nav.addItem(
                SideNavItem(
                    "Usuarios",
                    "users",
                    Icon(VaadinIcon.USERS)
                )
            )
        }

        nav.addItem(
            SideNavItem(
                "Reportes",
                "#",
                Icon(VaadinIcon.CHART)
            ).apply { isEnabled = false }
        )

        return nav
    }

    override fun afterNavigation() {
        super.afterNavigation()
        viewTitle.text = getCurrentPageTitle()
    }

    private fun getCurrentPageTitle(): String {
        val title = content.javaClass.getAnnotation(PageTitle::class.java)
        return title?.value ?: "Perro Amor"
    }
}