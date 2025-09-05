package com.perroamor.inventory.view

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed

@Route("catalogs", layout = MainLayout::class)
@PageTitle("Catálogos")
@RolesAllowed("ADMIN", "MANAGER")
class CatalogsView : VerticalLayout() {
    
    init {
        setSizeFull()
        setupView()
    }
    
    private fun setupView() {
        add(
            H1("📋 Gestión de Catálogos"),
            createDescription(),
            createCatalogsGrid()
        )
    }
    
    private fun createDescription(): Span {
        val description = Span("Administra los catálogos del sistema. Aquí puedes gestionar marcas, categorías y otros elementos maestros.")
        description.element.style.set("color", "var(--lumo-secondary-text-color)")
        description.element.style.set("margin-bottom", "24px")
        return description
    }
    
    private fun createCatalogsGrid(): VerticalLayout {
        val layout = VerticalLayout()
        layout.isSpacing = true
        layout.isPadding = false
        
        // Tarjeta de Marcas
        val brandsCard = createCatalogCard(
            title = "🏷️ Marcas",
            description = "Gestiona las marcas de productos disponibles en el inventario",
            route = "brands",
            icon = VaadinIcon.TAG,
            buttonText = "Gestionar Marcas"
        )
        
        // Aquí se pueden agregar más catálogos en el futuro
        /*
        val categoriesCard = createCatalogCard(
            title = "📁 Categorías", 
            description = "Administra las categorías de productos",
            route = "categories",
            icon = VaadinIcon.FOLDER,
            buttonText = "Gestionar Categorías"
        )
        */
        
        layout.add(brandsCard)
        // layout.add(categoriesCard) // Para futuras implementaciones
        
        return layout
    }
    
    private fun createCatalogCard(
        title: String,
        description: String,
        route: String,
        icon: VaadinIcon,
        buttonText: String
    ): HorizontalLayout {
        val card = HorizontalLayout()
        card.setWidthFull()
        card.element.style.set("border", "1px solid var(--lumo-contrast-10pct)")
        card.element.style.set("border-radius", "8px")
        card.element.style.set("padding", "24px")
        card.element.style.set("background", "var(--lumo-base-color)")
        card.element.style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
        card.element.style.set("transition", "box-shadow 0.3s ease")
        
        // Efecto hover
        card.element.addEventListener("mouseover") { _ ->
            card.element.style.set("box-shadow", "0 4px 8px rgba(0,0,0,0.15)")
        }
        card.element.addEventListener("mouseout") { _ ->
            card.element.style.set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
        }
        
        // Contenido izquierdo
        val contentLayout = VerticalLayout()
        contentLayout.isSpacing = false
        contentLayout.isPadding = false
        
        val titleSpan = H3(title)
        titleSpan.element.style.set("margin", "0 0 8px 0")
        titleSpan.element.style.set("color", "var(--lumo-primary-text-color)")
        
        val descriptionSpan = Span(description)
        descriptionSpan.element.style.set("color", "var(--lumo-secondary-text-color)")
        descriptionSpan.element.style.set("line-height", "1.4")
        
        contentLayout.add(titleSpan, descriptionSpan)
        
        // Botón de navegación
        val navigateButton = Button(buttonText, Icon(icon)) {
            UI.getCurrent().navigate(route)
        }
        navigateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        navigateButton.element.style.set("white-space", "nowrap")
        
        card.add(contentLayout, navigateButton)
        card.setFlexGrow(1.0, contentLayout)
        card.setFlexGrow(0.0, navigateButton)
        card.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, navigateButton)
        
        return card
    }
}