package com.perroamor.inventory.view

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.Header
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.sidenav.SideNav
import com.vaadin.flow.component.sidenav.SideNavItem
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.theme.lumo.LumoUtility

@PageTitle("Perro Amor - Sistema de Inventario")
class MainLayout : AppLayout() {

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

        val header = Header(toggle, viewTitle)
        header.addClassNames(
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Display.FLEX,
            LumoUtility.Padding.End.MEDIUM,
            LumoUtility.Width.FULL
        )

        addToNavbar(false, header)
    }

    private fun addDrawerContent() {
        val appName = Span("🐕 Perro Amor")
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE)

        val header = Header(appName)
        header.addClassNames(
            LumoUtility.AlignItems.CENTER,
            LumoUtility.Display.FLEX,
            LumoUtility.Padding.Horizontal.MEDIUM,
            LumoUtility.Padding.Vertical.MEDIUM
        )

        val scroller = Scroller(createNavigation())

        addToDrawer(header, scroller)
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