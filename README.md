# Wardrobe Manager (Java Swing)

A full-feature desktop wardrobe assistant built with Java Swing.

This project provides:
- user signup/login,
- wardrobe inventory management,
- AI styling recommendations,
- calendar event planning,
- weather-aware outfit context,
- donation suggestions for unused items,
- avatar management,
- profile management.

---

## 1) Project Overview

`Wardrobe Manager` is a personal fashion organizer where a user can:
1. create an account,
2. log in and choose city,
3. manage clothes by category,
4. get AI outfit recommendations using closet + weather + selected-date event context,
5. store events in calendar,
6. identify items eligible for donation,
7. manage avatar images,
8. manage profile image and account display.

---

## 2) Main Modules (Files)

- `signup.java` → account creation UI
- `login.java` → login UI + city selection
- `mainpage.java` → dashboard/home navigation hub
- `wardrobe.java` → closet management (`Shirts`, `Pants`, `Shoes`, `Cultural Dress`)
- `App.java` → AI Styling Assistant (OpenRouter integration)
- `calendar.java` → calendar + events + 7-day weather display (Open-Meteo)
- `DonationUI.java` → donation eligibility and item donation flow
- `AvatarApp.java` → avatar browser/add/delete
- `profile.java` → user profile and profile picture upload/delete
- `UserAccount.java`, `user.java` → account model and storage/search logic
- `clothing.java` → clothing data model logic used by wardrobe flows

---

## 3) End-to-End Navigation (Start from Signup)

### Step A: Create Account
- Run `signup.java`.
- Fill first name, last name, email, password, gender.
- Press **Sign In** (account is saved).
- Click **Already Have an account? Login** to go to login page.

### Step B: Login + Select City
- Run/continue into `login.java`.
- Enter email + password.
- On successful login, select city from the large city picker dialog.
- App opens the dashboard (`mainpage.java`).

### Step C: Dashboard (`mainpage.java`)
Available buttons:
- **Closet** → opens `wardrobe.java`
- **Styling Expert** → opens `App.java`
- **Donation** → opens `DonationUI.java`
- **Calendar** → opens `calendar.java`
- **Future Updates** → currently opens `AvatarApp.java`
- **Account** → opens `profile.java`

---

## 4) What Each Screen Provides

## 4.1 Signup (`signup.java`)
- Validates required fields.
- Validates email format.
- Enforces minimum password length.
- Prevents duplicate user email.
- Creates user data and initializes user directory flow.

## 4.2 Login (`login.java`)
- Authenticates using stored user data.
- Requires location selection (city).
- Passes `username`, `email`, and `city` into the dashboard context.

## 4.3 Dashboard (`mainpage.java`)
- Central navigation between all major modules.
- Uses safe defaults if args are missing (`GuestUser`, default email/city).

## 4.4 Closet Management (`wardrobe.java`)
- Category tabs: `Shirts`, `Pants`, `Shoes`, `Cultural Dress`.
- Add new item flow with large readable form/dialogs.
- Upload item image with enlarged file chooser support.
- Persists item metadata to per-category text files.
- Supports deleting stored items.
- Auto-creates required user folders/files.

## 4.5 AI Styling Assistant (`App.java`)
- Loads closet details and conversation history.
- Uses OpenRouter Chat Completions API (`openai/gpt-4o-mini`).
- Sends contextual prompt including:
  - user request,
  - current wardrobe inventory,
  - conversation history,
  - 7-day weather forecast,
  - selected date,
  - event stored on that selected date (if any).
- Includes **Select Date** picker in UI.
- **Add Event** button opens calendar with latest AI answer context.

## 4.6 Calendar + Weather + Events (`calendar.java`)
- Month view with previous/next navigation.
- Shows weather text on day cells for forecast days.
- Weather source: **Open-Meteo only** (7-day forecast).
- On day click:
  - open event editor,
  - add/overwrite event for selected date,
  - remove event.
- Stores events in monthly text files under user calendar folder.

## 4.7 Donation Suggestions (`DonationUI.java`)
- Scans wardrobe categories.
- Flags items eligible for donation (based on `Last Worn` age threshold).
- Displays eligible item image + quick details.
- Supports editing last-worn date.
- Supports donation confirmation and deletes donated item data/image.

## 4.8 Avatar Module (`AvatarApp.java`)
- Displays available avatar images.
- Add custom avatar image.
- Delete user-added avatars (prevents deleting default avatars).
- Persists avatar list in user avatar storage.

## 4.9 Profile (`profile.java`)
- Shows user name, email, gender.
- Circular profile picture display.
- Upload/delete profile picture.
- Stores profile image in user folder.

---

## 5) Data Storage Layout

Data is persisted under:

`~/WardrobeManagerData/<Username>/`

Typical structure:

- `Shirts/Shirts.txt` + shirt images
- `Pants/Pants.txt` + pant images
- `Shoes/Shoes.txt` + shoe images
- `Cultural Dress/Cultural Dress.txt` + images
- `Calendar/<Month><Year>.txt` (events)
- `Avatars/` (avatar images + avatar metadata file)
- profile image file(s)
- `geminiconversationhistory.txt` (AI chat history)

Shared user account records are managed via account files used by `UserAccount`.

---

## 6) How to Run

Because this is a plain Java Swing project (not Maven/Gradle in this folder), easiest way is VS Code Java extension run targets.

### Recommended (VS Code)
1. Open this folder in VS Code.
2. Run the class you want from the editor/run button.
3. Start journey from `signup.java`.

### Suggested order for full walkthrough
1. `signup.java`
2. `login.java`
3. `mainpage.java` (opens after successful login)
4. Visit each dashboard button/module.

---

## 7) Full User Journey Example

1. Create account in Signup.
2. Login with same email/password.
3. Choose city (e.g., Karachi).
4. In Dashboard:
   - Add wardrobe items in **Closet**.
   - Open **Calendar** and save event on a future date (e.g., wedding/interview).
   - Open **Styling Expert**:
     - choose same event date in date selector,
     - ask for outfit advice,
     - AI now considers closet + weather + that date’s event.
   - Open **Donation** to manage unused items.
   - Open **Account** to upload profile image.
   - Open **Future Updates** (Avatar) to manage avatars.

---

## 8) Key Functional Highlights

- End-to-end account flow (signup → login → dashboard).
- Centralized module launcher dashboard.
- Persistent local filesystem storage per user.
- AI styling augmented with weather + event context.
- Calendar-integrated planning with per-date event persistence.
- Donation eligibility workflow with update/delete actions.
- Avatar and profile personalization.
- Large, accessibility-improved UI controls/dialogs.

---

## 9) Notes / Operational Considerations

- Internet is required for:
  - AI Styling API calls,
  - Open-Meteo weather fetch.
- If AI API key is invalid/unavailable, styling requests will fail with API error.
- If weather API cannot be reached, forecast context may be empty in AI prompt.
- Run this project with the same user account/environment to access saved local data.

---

## 10) Suggested Future Improvements

- Move API keys to environment variables (avoid hardcoding).
- Add project build file (`pom.xml` or `build.gradle`) for reproducible builds.
- Add automated tests for account, storage, and event parsing logic.
- Add export/import backup for user wardrobe data.
- Add richer recommendation formatting (day-by-day weather outfit plans).

---

## 11) Quick File Map (for maintainers)

- Entry points: `signup`, `login`, `mainpage`, `App`, `calendar`, `wardrobe`, `DonationUI`, `AvatarApp`, `profile`
- Models/storage: `user`, `UserAccount`, `clothing`
- UI styling helpers are embedded in each module file (rounded borders, custom panels, etc.).

---

If you want, I can also generate a second `README_SHORT.md` (1-page quick start) for your submission/demo handoff.