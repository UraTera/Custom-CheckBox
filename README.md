## Fully customizable CheckBox. Four styles. Open source.

![CheckBox](https://github.com/user-attachments/assets/e252794e-8f93-4d9c-b112-e6b36c4f05db)

To use the ready-made library, add the dependency:
```
dependencies {

    implementation("io.github.uratera:check_box:1.0.1")
}
```

### Attributes
Attributes	|Description	|Default value
---------------------------------------|-------------------------------------------------|-------------------
check_checked	|Current state	|false
check_gravity	|Text layout (end, start)	|end
check_icon_Checked	|Icon in checked state	|nothing
check_icon_Unchecked	|Icon in unchecked state	|nothing
check_style	|Style	|square
check_text	|Text	|null
check_textColor	|Text color	|black
check_textSize	|Text size	|14sp
button_checkedColor	|Button color in checked state	|dark blue
button_uncheckedColor	|Border color in unchecked state	|dark gray
icon_uncheckedColor	|Icon color in unchecked state	|light gray
ripple_Color	|Ripple color	|light gray
tick_checkedColor	|Tick color in checked state	|light gray
tick_uncheckedColor	|Tick color in unchecked state	|white

**Styles:**
- square
- circle
- tick
- icon

**Usage:**
```
<com.tera.custom_checkbox.CheckBox
    android:id="@+id/checkBox"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:check_text="Text"/>
```

**State change listener:**

Kotlin
```
checkBox.setOnCheckedChangeListener { view, isChecked ->
    key = isChecked
}
```
Java
```
checkBox.setOnCheckedChangeListener((view, isChecked) -> {
    key = isChecked;
    return null;
});
```
**Methods:**
```
setChecked, isChecked
```

