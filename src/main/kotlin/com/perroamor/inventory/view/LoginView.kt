package com.perroamor.inventory.view

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.login.LoginI18n
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed

@Route("login")
@PageTitle("Iniciar Sesión | Perro Amor")
@AnonymousAllowed
class LoginView : VerticalLayout(), BeforeEnterObserver {

    private val loginForm = LoginForm()

    init {
        setSizeFull()
        setupLayout()
        setupLoginForm()
    }

    private fun setupLayout() {
        // Configurar el layout principal
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        alignItems = FlexComponent.Alignment.CENTER
        
        // Aplicar estilos de fondo
        element.style.apply {
            set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
            set("min-height", "100vh")
        }

        // Container principal
        val container = createLoginContainer()
        add(container)
    }

    private fun createLoginContainer(): VerticalLayout {
        val container = VerticalLayout()
        container.width = "400px"
        container.isPadding = true
        container.isSpacing = true
        
        // Estilos del container
        container.element.style.apply {
            set("background", "white")
            set("border-radius", "12px")
            set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)")
            set("padding", "40px")
        }

        // Header con logo/título
        val header = createHeader()
        
        container.add(header, loginForm)
        container.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, header, loginForm)
        
        return container
    }

    private fun createHeader(): VerticalLayout {
        val headerLayout = VerticalLayout()
        headerLayout.isSpacing = false
        headerLayout.isPadding = false
        headerLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER)

        // Logo/Icon
        val logo = Span("🐕")
        logo.element.style.apply {
            set("font-size", "4rem")
            set("margin-bottom", "16px")
        }

        // Título
        val title = H1("Perro Amor")
        title.element.style.apply {
            set("margin", "0")
            set("color", "#333")
            set("font-weight", "bold")
            set("text-align", "center")
        }

        // Subtítulo
        val subtitle = Span("Sistema de Inventario y Ventas")
        subtitle.element.style.apply {
            set("color", "#666")
            set("font-size", "1.1rem")
            set("margin-bottom", "32px")
            set("text-align", "center")
        }

        headerLayout.add(logo, title, subtitle)
        return headerLayout
    }

    private fun createCredentialsInfo(): Div {
        val infoDiv = Div()
        infoDiv.element.style.apply {
            set("background", "#f8f9fa")
            set("border", "1px solid #e9ecef")
            set("border-radius", "8px")
            set("padding", "16px")
            set("margin-bottom", "24px")
            set("text-align", "center")
        }

        val infoTitle = Span("💡 Credenciales por defecto")
        infoTitle.element.style.apply {
            set("font-weight", "bold")
            set("color", "#495057")
            set("display", "block")
            set("margin-bottom", "8px")
        }

        val userInfo = Span("Usuario: admin")
        userInfo.element.style.apply {
            set("display", "block")
            set("color", "#6c757d")
            set("margin-bottom", "4px")
        }

        val passInfo = Span("Contraseña: admin123")
        passInfo.element.style.apply {
            set("display", "block")
            set("color", "#6c757d")
        }

        infoDiv.add(infoTitle, userInfo, passInfo)
        return infoDiv
    }

    private fun setupLoginForm() {
        // Configurar textos en español
        val i18n = LoginI18n.createDefault()
        i18n.form.title = "Iniciar Sesión"
        i18n.form.username = "Usuario"
        i18n.form.password = "Contraseña" 
        i18n.form.submit = "Iniciar Sesión"
        i18n.form.forgotPassword = "¿Olvidaste tu contraseña?"
        
        i18n.errorMessage.title = "Error de autenticación"
        i18n.errorMessage.message = "Usuario o contraseña incorrectos. Por favor, intenta de nuevo."
        i18n.errorMessage.username = "El usuario es requerido"
        i18n.errorMessage.password = "La contraseña es requerida"

        loginForm.setI18n(i18n)
        loginForm.action = "login"
        loginForm.isForgotPasswordButtonVisible = false

        // Estilos personalizados para el formulario
        loginForm.element.style.apply {
            set("width", "100%")
        }

        // Personalizar el botón de login
        loginForm.element.executeJs("""
            const submitButton = this.shadowRoot.querySelector('vaadin-button[part="vaadin-login-submit"]');
            if (submitButton) {
                submitButton.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
                submitButton.style.border = 'none';
                submitButton.style.borderRadius = '8px';
                submitButton.style.fontSize = '1.1rem';
                submitButton.style.fontWeight = 'bold';
                submitButton.style.padding = '12px 24px';
                submitButton.style.marginTop = '16px';
            }
        """)
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        // Mostrar mensaje de error si existe
        if (event.location.queryParameters.parameters.containsKey("error")) {
            loginForm.isError = true
        }
    }
}