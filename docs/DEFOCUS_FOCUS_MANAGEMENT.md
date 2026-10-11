# Defocus Architecture & Focus Management ("Defocion")

## 1. Problem Statement

In rich Android Jetpack Compose forms (such as invoice line item editors, client profile sheets, and search bars), the virtual keyboard often remains stuck on the screen when the user taps outside the active `TextField`. By default in Compose:
- Simply tapping outside an active text field on a parent `Box` or `Column` does not automatically transfer or clear focus.
- The `KeyboardController?.hide()` only hides the keyboard, but leaves the cursor blinking and focus locked, which causes confusing re-expansion on next touch.

---

## 2. The Solution: `clearFocusOnTap` Extension

We created `com.example.ui.util.FocusUtils.kt` to solve this holistically:

```kotlin
package com.example.ui.util

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Defocus utility for Jetpack Compose.
 * Automatically clears active focus and dismisses the soft keyboard
 * when tapping outside input controls.
 */
fun Modifier.clearFocusOnTap(focusManager: FocusManager): Modifier {
    return this.pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                focusManager.clearFocus()
            }
        )
    }
}
```

### Why this works:
1. `pointerInput(Unit)` with `detectTapGestures(onTap = { ... })` intercepts taps that are not consumed by interactive children (such as buttons or text fields).
2. `focusManager.clearFocus()` cleanly releases the software keyboard and resets the focus tree, ensuring that:
   - The cursor stops blinking.
   - The virtual keyboard collapses smoothly.
   - Screen space is restored for reviewing ledger data.

---

## 3. Integration Points

| File | Integration Target | Benefit |
| :--- | :--- | :--- |
| `MainActivity.kt` | Root `Scaffold` container | Global fallback defocusing for all screens |
| `LoginScreen.kt` | Root `Box` & inner `Column` | Defocuses when clicking outside email/password |
| `InvoicesScreen.kt` | Search bar header & item list | Dismisses keyboard when scrolling or filtering |
| `ClientsScreen.kt` | Table & Card filter containers | Clean table view navigation without keyboard clutter |
| `PayoutsScreen.kt` | `RequestPayoutBottomSheet` | Dismisses keyboard when changing SEPA IBAN or amounts |

---

## 4. Debugging & Testing Focus Behavior

1. **Manual Verification**: Tap on any text input (e.g. Search Bar on Invoices Screen). When keyboard appears, tap on empty whitespace between cards or on the screen title. The keyboard should collapse immediately and the search bar cursor should disappear.
2. **Automated Compose Test**:
```kotlin
@Test
fun testDefocusOnTapOutside() {
    composeTestRule.setContent {
        MyScreenWithInput()
    }
    composeTestRule.onNodeWithTag("search_input").performTextInput("Invoice")
    composeTestRule.onNodeWithTag("search_input").assertIsFocused()
    
    // Tap on parent container
    composeTestRule.onNodeWithTag("parent_container").performClick()
    composeTestRule.onNodeWithTag("search_input").assertIsNotFocused()
}
```
