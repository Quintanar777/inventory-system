package com.perroamor.inventory.view

import com.perroamor.inventory.entity.User
import com.perroamor.inventory.entity.Role
import com.perroamor.inventory.service.UserService
import com.perroamor.inventory.service.RoleService
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.EmailField
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired
import jakarta.annotation.security.RolesAllowed
import java.time.format.DateTimeFormatter

@Route("users", layout = MainLayout::class)
@PageTitle("Gestión de Usuarios")
@RolesAllowed("ADMIN")
class UserManagementView(
    @Autowired private val userService: UserService,
    @Autowired private val roleService: RoleService
) : VerticalLayout() {
    
    private val grid = Grid(User::class.java)
    private val usernameField = TextField("Usuario")
    private val emailField = EmailField("Email")
    private val fullNameField = TextField("Nombre Completo")
    private val passwordField = PasswordField("Contraseña")
    private val confirmPasswordField = PasswordField("Confirmar Contraseña")
    private val roleField = ComboBox<Role>("Rol")
    private val activeField = Checkbox("Usuario Activo")
    
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    
    init {
        setSizeFull()
        configureGrid()
        setupRoleField()
        
        add(
            createHeader(),
            createToolbar(),
            grid
        )
        
        updateList()
    }
    
    private fun createHeader(): VerticalLayout {
        val layout = VerticalLayout()
        layout.isSpacing = false
        layout.isPadding = false
        
        val title = H1("👥 Gestión de Usuarios")
        title.element.style.set("margin", "0 0 8px 0")
        
        val description = Span("Administra los usuarios del sistema y sus roles de acceso.")
        description.element.style.set("color", "var(--lumo-secondary-text-color)")
        description.element.style.set("margin-bottom", "16px")
        
        layout.add(title, description)
        return layout
    }
    
    private fun createToolbar(): HorizontalLayout {
        val addButton = Button("Nuevo Usuario") { addUser() }
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        addButton.icon = Icon(VaadinIcon.PLUS)
        
        val refreshButton = Button("Actualizar") { updateList() }
        refreshButton.icon = Icon(VaadinIcon.REFRESH)
        
        val toolbar = HorizontalLayout(addButton, refreshButton)
        toolbar.element.style.set("margin-bottom", "16px")
        
        return toolbar
    }
    
    private fun setupRoleField() {
        val roles = roleService.findAll()
        roleField.setItems(roles)
        roleField.setItemLabelGenerator { "${it.name} - ${it.description}" }
    }
    
    private fun configureGrid() {
        grid.setSizeFull()
        grid.removeAllColumns()
        
        // Columna ID
        grid.addColumn { it.id }
            .setHeader("ID")
            .setWidth("80px")
            .setFlexGrow(0)
        
        // Columna Usuario
        grid.addColumn { it.username }
            .setHeader("Usuario")
            .setFlexGrow(1)
        
        // Columna Nombre Completo
        grid.addColumn { it.fullName }
            .setHeader("Nombre Completo")
            .setFlexGrow(2)
        
        // Columna Email
        grid.addColumn { it.email }
            .setHeader("Email")
            .setFlexGrow(2)
        
        // Columna Rol
        grid.addColumn { it.role.name }
            .setHeader("Rol")
            .setWidth("120px")
            .setFlexGrow(0)
        
        // Columna Estado
        grid.addColumn { user ->
            if (user.isActive) "✅ Activo" else "❌ Inactivo"
        }.setHeader("Estado").setWidth("100px").setFlexGrow(0)
        
        // Columna Último Login
        grid.addColumn { user ->
            user.lastLogin?.format(dateFormatter) ?: "Nunca"
        }.setHeader("Último Acceso").setWidth("150px").setFlexGrow(0)
        
        // Columna Fecha Creación
        grid.addColumn { user ->
            user.createdAt.format(dateFormatter)
        }.setHeader("Creado").setWidth("150px").setFlexGrow(0)
        
        // Columna de acciones
        grid.addComponentColumn { user ->
            createActionButtons(user)
        }.setHeader("Acciones").setWidth("200px").setFlexGrow(0)
        
        grid.asSingleSelect().addValueChangeListener { event ->
            event.value?.let { editUser(it) }
        }
    }
    
    private fun createActionButtons(user: User): HorizontalLayout {
        val layout = HorizontalLayout()
        layout.isSpacing = true
        
        val editButton = Button(Icon(VaadinIcon.EDIT)) {
            editUser(user)
        }
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY)
        editButton.element.setAttribute("aria-label", "Editar usuario")
        
        val toggleButton = if (user.isActive) {
            Button(Icon(VaadinIcon.BAN)) {
                toggleUserStatus(user, false)
            }.apply {
                addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY)
                element.setAttribute("aria-label", "Desactivar usuario")
            }
        } else {
            Button(Icon(VaadinIcon.CHECK_CIRCLE)) {
                toggleUserStatus(user, true)
            }.apply {
                addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY)
                element.setAttribute("aria-label", "Activar usuario")
            }
        }
        
        val resetPasswordButton = Button(Icon(VaadinIcon.KEY)) {
            resetPassword(user)
        }
        resetPasswordButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY)
        resetPasswordButton.element.setAttribute("aria-label", "Resetear contraseña")
        
        layout.add(editButton, toggleButton, resetPasswordButton)
        return layout
    }
    
    private fun addUser() {
        showUserDialog(null)
    }
    
    private fun editUser(user: User) {
        showUserDialog(user)
    }
    
    private fun showUserDialog(user: User?) {
        val dialog = Dialog()
        dialog.width = "600px"
        
        val formLayout = FormLayout()
        
        // Configurar campos
        if (user != null) {
            usernameField.value = user.username
            emailField.value = user.email
            fullNameField.value = user.fullName
            roleField.value = user.role
            activeField.value = user.isActive
            
            // Para edición, no mostrar campos de contraseña
            passwordField.isVisible = false
            confirmPasswordField.isVisible = false
        } else {
            clearForm()
            // Para nuevo usuario, mostrar campos de contraseña
            passwordField.isVisible = true
            confirmPasswordField.isVisible = true
        }
        
        // Validaciones
        usernameField.isRequiredIndicatorVisible = true
        emailField.isRequiredIndicatorVisible = true
        fullNameField.isRequiredIndicatorVisible = true
        
        if (user == null) {
            passwordField.isRequiredIndicatorVisible = true
            confirmPasswordField.isRequiredIndicatorVisible = true
        }
        
        formLayout.add(
            usernameField, emailField, fullNameField, 
            passwordField, confirmPasswordField, 
            roleField, activeField
        )
        
        // Botones
        val saveButton = Button("Guardar") {
            if (validateAndSaveUser(user)) {
                dialog.close()
            }
        }
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        
        val cancelButton = Button("Cancelar") { 
            dialog.close() 
        }
        
        val buttonLayout = HorizontalLayout(saveButton, cancelButton)
        
        val mainLayout = VerticalLayout(
            if (user != null) H3("Editar Usuario") else H3("Nuevo Usuario"),
            formLayout,
            buttonLayout
        )
        
        dialog.add(mainLayout)
        dialog.open()
    }
    
    private fun validateAndSaveUser(existingUser: User?): Boolean {
        try {
            // Validaciones básicas
            if (usernameField.value.isBlank()) {
                Notification.show("El nombre de usuario es requerido", 3000, Notification.Position.TOP_CENTER)
                return false
            }
            
            if (emailField.value.isBlank()) {
                Notification.show("El email es requerido", 3000, Notification.Position.TOP_CENTER)
                return false
            }
            
            if (fullNameField.value.isBlank()) {
                Notification.show("El nombre completo es requerido", 3000, Notification.Position.TOP_CENTER)
                return false
            }
            
            if (roleField.value == null) {
                Notification.show("Debe seleccionar un rol", 3000, Notification.Position.TOP_CENTER)
                return false
            }
            
            // Validación de contraseñas para nuevo usuario
            if (existingUser == null) {
                if (passwordField.value.isBlank()) {
                    Notification.show("La contraseña es requerida", 3000, Notification.Position.TOP_CENTER)
                    return false
                }
                
                if (passwordField.value != confirmPasswordField.value) {
                    Notification.show("Las contraseñas no coinciden", 3000, Notification.Position.TOP_CENTER)
                    return false
                }
                
                if (passwordField.value.length < 6) {
                    Notification.show("La contraseña debe tener al menos 6 caracteres", 3000, Notification.Position.TOP_CENTER)
                    return false
                }
            }
            
            // Verificar username único (solo para nuevo usuario o si cambió)
            if (existingUser == null || existingUser.username != usernameField.value) {
                if (userService.existsByUsername(usernameField.value)) {
                    Notification.show("Ya existe un usuario con ese nombre", 3000, Notification.Position.TOP_CENTER)
                    return false
                }
            }
            
            // Verificar email único (solo para nuevo usuario o si cambió)
            if (existingUser == null || existingUser.email != emailField.value) {
                if (userService.existsByEmail(emailField.value)) {
                    Notification.show("Ya existe un usuario con ese email", 3000, Notification.Position.TOP_CENTER)
                    return false
                }
            }
            
            // Crear o actualizar usuario
            if (existingUser != null) {
                // Actualizar usuario existente
                userService.updateUser(
                    existingUser.id,
                    usernameField.value,
                    emailField.value,
                    fullNameField.value,
                    roleField.value!!,
                    activeField.value
                )
                Notification.show("Usuario actualizado exitosamente", 3000, Notification.Position.TOP_CENTER)
            } else {
                // Crear nuevo usuario
                userService.createUser(
                    usernameField.value,
                    passwordField.value,
                    emailField.value,
                    fullNameField.value,
                    roleField.value!!,
                    activeField.value
                )
                Notification.show("Usuario creado exitosamente", 3000, Notification.Position.TOP_CENTER)
            }
            
            updateList()
            clearForm()
            return true
            
        } catch (e: Exception) {
            Notification.show("Error: ${e.message}", 3000, Notification.Position.TOP_CENTER)
            return false
        }
    }
    
    private fun toggleUserStatus(user: User, newStatus: Boolean) {
        try {
            userService.updateUserStatus(user.id, newStatus)
            updateList()
            
            val message = if (newStatus) "Usuario activado" else "Usuario desactivado"
            Notification.show(message, 2000, Notification.Position.TOP_CENTER)
        } catch (e: Exception) {
            Notification.show("Error: ${e.message}", 3000, Notification.Position.TOP_CENTER)
        }
    }
    
    private fun resetPassword(user: User) {
        val dialog = Dialog()
        dialog.width = "400px"
        
        val newPasswordField = PasswordField("Nueva Contraseña")
        val confirmNewPasswordField = PasswordField("Confirmar Nueva Contraseña")
        
        newPasswordField.isRequiredIndicatorVisible = true
        confirmNewPasswordField.isRequiredIndicatorVisible = true
        
        val resetButton = Button("Resetear Contraseña") {
            if (newPasswordField.value.isBlank()) {
                Notification.show("La contraseña es requerida", 3000, Notification.Position.TOP_CENTER)
                return@Button
            }
            
            if (newPasswordField.value != confirmNewPasswordField.value) {
                Notification.show("Las contraseñas no coinciden", 3000, Notification.Position.TOP_CENTER)
                return@Button
            }
            
            if (newPasswordField.value.length < 6) {
                Notification.show("La contraseña debe tener al menos 6 caracteres", 3000, Notification.Position.TOP_CENTER)
                return@Button
            }
            
            try {
                userService.resetPassword(user.id, newPasswordField.value)
                Notification.show("Contraseña actualizada exitosamente", 3000, Notification.Position.TOP_CENTER)
                dialog.close()
            } catch (e: Exception) {
                Notification.show("Error: ${e.message}", 3000, Notification.Position.TOP_CENTER)
            }
        }
        resetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        
        val cancelButton = Button("Cancelar") { dialog.close() }
        
        val layout = VerticalLayout(
            H3("Resetear Contraseña - ${user.username}"),
            newPasswordField,
            confirmNewPasswordField,
            HorizontalLayout(resetButton, cancelButton)
        )
        
        dialog.add(layout)
        dialog.open()
    }
    
    private fun clearForm() {
        usernameField.clear()
        emailField.clear()
        fullNameField.clear()
        passwordField.clear()
        confirmPasswordField.clear()
        roleField.clear()
        activeField.value = true
    }
    
    private fun updateList() {
        grid.setItems(userService.findAll())
    }
}