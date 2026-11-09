// ==========================================
// ANIMAL FARM MANAGEMENT SYSTEM - SCRIPT.JS
// Connected to Database & Real-Time Sync
// ==========================================

// Initial sample seed records if storage is clean
const SAMPLE_ANIMALS = [
    { tag: "AN-101", name: "Daisy", species: "Cattle", breed: "Holstein", age: "3.5 yrs", gender: "Female", health: "Healthy" },
    { tag: "AN-102", name: "Barnaby", species: "Sheep", breed: "Merino", age: "2 yrs", gender: "Male", health: "Healthy" },
    { tag: "AN-103", name: "Billy", species: "Goat", breed: "Boer", age: "1.5 yrs", gender: "Male", health: "Under Treatment" },
    { tag: "AN-104", name: "Thunder", species: "Horse", breed: "Quarter Horse", age: "5 yrs", gender: "Male", health: "Healthy" },
    { tag: "AN-105", name: "Pippa", species: "Pig", breed: "Yorkshire", age: "1 yr", gender: "Female", health: "Healthy" },
    { tag: "AN-106", name: "Rusty", species: "Poultry", breed: "Rhode Island Red", age: "8 mos", gender: "Female", health: "Quarantine" }
];

const SAMPLE_FEED = [
    { code: "FD-01", name: "Alfalfa Green Hay", stock: 450, consumption: 25, updated: "2026-10-06" },
    { code: "FD-02", name: "Cattle Protein Pellets", stock: 120, consumption: 28, updated: "2026-10-05" },
    { code: "FD-03", name: "Organic Grain Blend", stock: 65, consumption: 20, updated: "2026-10-07" },
    { code: "FD-04", name: "Mineral & Salt Lick", stock: 85, consumption: 4, updated: "2026-10-04" }
];

const SAMPLE_HEALTH = [
    { id: "HL-201", animal: "AN-101 (Daisy)", type: "Annual Anthrax Booster", vet: "Dr. Miller", date: "2026-09-28", status: "Completed", notes: "Normal vital signs after vaccination." },
    { id: "HL-202", animal: "AN-103 (Billy)", type: "Hoof Rot Treatment", vet: "Dr. Sarah Lee", date: "2026-10-03", status: "Pending", notes: "Topical copper sulfate applied; re-check in 5 days." },
    { id: "HL-203", animal: "AN-106 (Rusty)", type: "Avian Respiratory Isolation", vet: "Dr. Miller", date: "2026-10-05", status: "Pending", notes: "Placed in quarantine coop #3." }
];

const SAMPLE_USERS = [
    { username: "admin", role: "ADMIN", fullName: "System Administrator", email: "admin@farm.local", farmType: "Headquarters" },
    { username: "farmer1", role: "USER", fullName: "Farmer 1", email: "farmer1@farm.local", farmType: "Dairy & Cattle" }
];

// Local cache storage helpers
function getStoredUsers() {
    try {
        const stored = localStorage.getItem("afms_users");
        if (stored) return JSON.parse(stored);
    } catch (e) {
        console.warn("Storage error", e);
    }
    return SAMPLE_USERS;
}

function saveUsers(users) {
    try {
        localStorage.setItem("afms_users", JSON.stringify(users));
    } catch (e) {
        console.warn("Storage save error", e);
    }
}

function getStoredAnimals() {
    try {
        const stored = localStorage.getItem("afms_animals");
        if (stored) return JSON.parse(stored);
    } catch (e) {
        console.warn(e);
    }
    return SAMPLE_ANIMALS;
}

function saveAnimals(animals) {
    try {
        localStorage.setItem("afms_animals", JSON.stringify(animals));
    } catch (e) {
        console.warn(e);
    }
}

function getStoredFeed() {
    try {
        const stored = localStorage.getItem("afms_feed");
        if (stored) return JSON.parse(stored);
    } catch (e) {
        console.warn(e);
    }
    return SAMPLE_FEED;
}

function saveFeed(feed) {
    try {
        localStorage.setItem("afms_feed", JSON.stringify(feed));
    } catch (e) {
        console.warn(e);
    }
}

function getStoredHealth() {
    try {
        const stored = localStorage.getItem("afms_health");
        if (stored) return JSON.parse(stored);
    } catch (e) {
        console.warn(e);
    }
    return SAMPLE_HEALTH;
}

function saveHealth(health) {
    try {
        localStorage.setItem("afms_health", JSON.stringify(health));
    } catch (e) {
        console.warn(e);
    }
}

// Current session management
function setCurrentSession(username, role) {
    sessionStorage.setItem("afms_currentUser", JSON.stringify({ username, role }));
}

function getCurrentSession() {
    try {
        const session = sessionStorage.getItem("afms_currentUser");
        return session ? JSON.parse(session) : null;
    } catch (e) {
        return null;
    }
}

// Alert banner notifications
function showAlert(message, type = "error") {
    const banner = document.getElementById("alertBanner");
    if (!banner) {
        alert(message.replace(/<[^>]*>?/gm, ""));
        return;
    }
    banner.className = `alert-banner ${type}`;
    banner.innerHTML = message;
    banner.style.display = "block";
    banner.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function hideAlert() {
    const banner = document.getElementById("alertBanner");
    if (banner) {
        banner.style.display = "none";
    }
}

// Password toggle helper
function togglePasswordVisibility(inputId, toggleBtn) {
    const input = document.getElementById(inputId);
    if (!input) return;
    if (input.type === "password") {
        input.type = "text";
        if (toggleBtn) toggleBtn.textContent = "🙈";
    } else {
        input.type = "password";
        if (toggleBtn) toggleBtn.textContent = "👁️";
    }
}

// Switch between User and Admin tab on login.html
function switchLoginRole(role) {
    hideAlert();
    const selectedRoleInput = document.getElementById("selectedRole");
    const tabUser = document.getElementById("tabUser");
    const tabAdmin = document.getElementById("tabAdmin");
    const title = document.getElementById("loginTitle");
    const subtitle = document.getElementById("loginSubtitle");
    const badge = document.getElementById("portalBadge");
    const submitBtn = document.getElementById("submitBtn");
    const demoHintText = document.getElementById("demoHintText");

    if (selectedRoleInput) selectedRoleInput.value = role;

    if (role === "ADMIN") {
        if (tabUser) tabUser.className = "role-tab";
        if (tabAdmin) tabAdmin.className = "role-tab active admin-active";
        if (title) title.textContent = "Admin Portal Login";
        if (subtitle) subtitle.textContent = "Secure administrative access to farm records & management.";
        if (badge) badge.textContent = "🛡️";
        if (submitBtn) {
            submitBtn.className = "btn admin";
            submitBtn.innerHTML = "<span>🔐 Sign In as Admin</span>";
        }
        if (demoHintText) {
            demoHintText.innerHTML = "💡 Admin Credentials: <strong>admin</strong> / <strong>admin123</strong>";
        }
    } else {
        if (tabUser) tabUser.className = "role-tab active";
        if (tabAdmin) tabAdmin.className = "role-tab";
        if (title) title.textContent = "User Portal Login";
        if (subtitle) subtitle.textContent = "Welcome back! Access your animal farm management dashboard.";
        if (badge) badge.textContent = "🚜";
        if (submitBtn) {
            submitBtn.className = "btn";
            submitBtn.innerHTML = "<span>🔓 Sign In</span>";
        }
        if (demoHintText) {
            demoHintText.innerHTML = "💡 Demo User: <strong>farmer1</strong> / <strong>password123</strong>";
        }
    }
}

// Auto-fill demo credentials
function fillDemoCredentials() {
    const role = document.getElementById("selectedRole") ? document.getElementById("selectedRole").value : "USER";
    const usernameInput = document.getElementById("loginUsername") || document.getElementById("adminUsername");
    const passwordInput = document.getElementById("loginPassword") || document.getElementById("adminPassword");

    if (role === "ADMIN") {
        if (usernameInput) usernameInput.value = "admin";
        if (passwordInput) passwordInput.value = "admin123";
    } else {
        if (usernameInput) usernameInput.value = "farmer1";
        if (passwordInput) passwordInput.value = "password123";
    }
    showAlert("Demo credentials loaded into form!", "info");
}

// ================= LOGIN HANDLERS =================

// Handle Login Form Submission on login.html
function handleLoginSubmit(event) {
    event.preventDefault();
    hideAlert();

    const usernameInput = document.getElementById("loginUsername");
    const passwordInput = document.getElementById("loginPassword");
    const roleInput = document.getElementById("selectedRole");
    const submitBtn = document.getElementById("submitBtn");

    const username = usernameInput ? usernameInput.value.trim() : "";
    const password = passwordInput ? passwordInput.value : "";
    const role = roleInput ? roleInput.value : "USER";

    if (!username) {
        showAlert("Please enter your username.", "error");
        return false;
    }

    if (!password) {
        showAlert("Please enter your password.", "error");
        return false;
    }

    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = "<span>⏳ Signing in...</span>";
    }

    // Call server login endpoint
    fetch("login", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ username, password, role })
    })
        .then(response => {
            if (!response.ok && response.status !== 400 && response.status !== 401) {
                throw new Error("Server returned HTTP " + response.status);
            }
            return response.json();
        })
        .then(result => {
            if (!result.success) {
                throw new Error(result.message || "Invalid username or password. Please try again.");
            }

            const activeRole = result.role || role;
            showAlert("Login successful! Welcome, " + (result.username || username) + "!", "success");
            setCurrentSession(result.username || username, activeRole);

            setTimeout(() => {
                window.location.href = "user-dashboard.html";
            }, 600);
        })
        .catch(error => {
            // Check fallback for offline/file:// mode
            const isOffline = window.location.protocol === "file:" || error.message.includes("Failed to fetch");
            if (isOffline) {
                const isAdmin = role === "ADMIN" && username.toLowerCase() === "admin" && password === "admin123";
                const storedUsers = getStoredUsers();
                const matchedUser = storedUsers.find(u => u.username.toLowerCase() === username.toLowerCase() && u.password === password);

                if (isAdmin || matchedUser) {
                    const activeRole = isAdmin ? "ADMIN" : (matchedUser.role || "USER");
                    showAlert("Login successful (Local Mode)! Welcome, " + username + "!", "success");
                    setCurrentSession(username, activeRole);
                    setTimeout(() => {
                        window.location.href = "user-dashboard.html";
                    }, 600);
                    return;
                }
            }

            showAlert(error.message || "Invalid credentials. Please verify your username and password.", "error");

            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = role === "ADMIN"
                    ? "<span>🔐 Sign In as Admin</span>"
                    : "<span>🔓 Sign In</span>";
            }
        });

    return false;
}

// Handle Admin Login on admin-login.html
function validateAdminLogin(event) {
    if (event) event.preventDefault();
    hideAlert();

    const usernameInput = document.getElementById("adminUsername");
    const passwordInput = document.getElementById("adminPassword");

    const username = usernameInput ? usernameInput.value.trim() : "";
    const password = passwordInput ? passwordInput.value : "";

    if (!username || !password) {
        showAlert("Please fill in both admin username and password.", "error");
        return false;
    }

    fetch("login", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ username, password, role: "ADMIN" })
    })
        .then(response => response.json())
        .then(result => {
            if (!result.success) {
                throw new Error(result.message || "Invalid admin credentials. Default is admin / admin123.");
            }
            showAlert("Admin authenticated successfully! Redirecting...", "success");
            setCurrentSession(result.username || "Administrator", "ADMIN");
            setTimeout(() => {
                window.location.href = "user-dashboard.html";
            }, 600);
        })
        .catch(error => {
            const isOffline = window.location.protocol === "file:" || error.message.includes("Failed to fetch");
            if (isOffline && username.toLowerCase() === "admin" && password === "admin123") {
                showAlert("Admin authenticated (Local Mode)! Redirecting...", "success");
                setCurrentSession("Administrator", "ADMIN");
                setTimeout(() => {
                    window.location.href = "user-dashboard.html";
                }, 600);
                return;
            }
            showAlert(error.message || "Invalid admin credentials. Default is admin / admin123.", "error");
        });

    return false;
}

// Password strength evaluator for registration
function handlePasswordInput() {
    const password = document.getElementById("password") ? document.getElementById("password").value : "";
    const bar1 = document.getElementById("strBar1");
    const bar2 = document.getElementById("strBar2");
    const bar3 = document.getElementById("strBar3");
    const strLabel = document.getElementById("strLabel");

    if (!bar1 || !bar2 || !bar3 || !strLabel) return;

    if (!password) {
        bar1.style.backgroundColor = "transparent";
        bar2.style.backgroundColor = "transparent";
        bar3.style.backgroundColor = "transparent";
        strLabel.textContent = "Enter password";
        strLabel.style.color = "#64748b";
        handleConfirmPasswordInput();
        return;
    }

    let score = 0;
    if (password.length >= 4) score++;
    if (password.length >= 7 && /[A-Z0-9]/.test(password)) score++;
    if (password.length >= 8 && /[^A-Za-z0-9]/.test(password)) score++;

    if (score === 1) {
        bar1.style.backgroundColor = "#ef4444";
        bar2.style.backgroundColor = "transparent";
        bar3.style.backgroundColor = "transparent";
        strLabel.textContent = "Weak";
        strLabel.style.color = "#ef4444";
    } else if (score === 2) {
        bar1.style.backgroundColor = "#f59e0b";
        bar2.style.backgroundColor = "#f59e0b";
        bar3.style.backgroundColor = "transparent";
        strLabel.textContent = "Good";
        strLabel.style.color = "#d97706";
    } else {
        bar1.style.backgroundColor = "#10b981";
        bar2.style.backgroundColor = "#10b981";
        bar3.style.backgroundColor = "#10b981";
        strLabel.textContent = "Strong";
        strLabel.style.color = "#059669";
    }

    handleConfirmPasswordInput();
}

function handleConfirmPasswordInput() {
    const password = document.getElementById("password") ? document.getElementById("password").value : "";
    const confirmInput = document.getElementById("confirmPassword");
    const matchHint = document.getElementById("matchHint");

    if (!confirmInput || !matchHint) return;

    const confirmVal = confirmInput.value;

    if (!confirmVal) {
        matchHint.className = "match-hint";
        matchHint.textContent = "";
        return;
    }

    if (password === confirmVal) {
        matchHint.className = "match-hint matched";
        matchHint.textContent = "Passwords match";
    } else {
        matchHint.className = "match-hint mismatched";
        matchHint.textContent = "✗ Passwords do not match";
    }
}

// Auto-fill sample data for registration
function fillSampleRegister() {
    const sampleNames = ["John Miller", "Sarah Jenkins", "Robert Davis", "Emma Wilson"];
    const randomIdx = Math.floor(Math.random() * sampleNames.length);
    const chosenName = sampleNames[randomIdx];
    const userSlug = chosenName.toLowerCase().replace(" ", "_");

    if (document.getElementById("fullName")) document.getElementById("fullName").value = chosenName;
    if (document.getElementById("username")) document.getElementById("username").value = userSlug + (Math.floor(Math.random() * 89) + 10);
    if (document.getElementById("email")) document.getElementById("email").value = `${userSlug}@greenpastures.com`;
    if (document.getElementById("farmType")) document.getElementById("farmType").selectedIndex = (randomIdx % 4) + 1;
    if (document.getElementById("password")) document.getElementById("password").value = "FarmPass#2026";
    if (document.getElementById("confirmPassword")) document.getElementById("confirmPassword").value = "FarmPass#2026";
    if (document.getElementById("agreeTerms")) document.getElementById("agreeTerms").checked = true;

    handlePasswordInput();
    handleConfirmPasswordInput();
    showAlert("Sample farm account details pre-filled!", "info");
}

// Handle Registration Form Submission
function validateRegistration(event) {
    if (event) event.preventDefault();
    hideAlert();

    const fullName = document.getElementById("fullName") ? document.getElementById("fullName").value.trim() : "";
    const username = document.getElementById("username") ? document.getElementById("username").value.trim() : "";
    const email = document.getElementById("email") ? document.getElementById("email").value.trim() : "";
    const farmType = document.getElementById("farmType") ? document.getElementById("farmType").value : "General Livestock";
    const password = document.getElementById("password") ? document.getElementById("password").value : "";
    const confirmPassword = document.getElementById("confirmPassword") ? document.getElementById("confirmPassword").value : "";
    const agreeTerms = document.getElementById("agreeTerms");
    const regSubmitBtn = document.getElementById("regSubmitBtn");

    if (!username) {
        showAlert("Please choose a username.", "error");
        return false;
    }

    if (username.length < 3) {
        showAlert("Username must be at least 3 characters long.", "error");
        return false;
    }

    if (!/^[a-zA-Z0-9_]+$/.test(username)) {
        showAlert("Username can only contain letters, numbers, and underscores (_).", "error");
        return false;
    }

    if (username.toLowerCase() === "admin") {
        showAlert("The username 'admin' is reserved for the system administrator.", "error");
        return false;
    }

    if (!password) {
        showAlert("Please create a password.", "error");
        return false;
    }

    if (password.length < 4) {
        showAlert("Password must contain at least 4 characters.", "error");
        return false;
    }

    if (password !== confirmPassword) {
        showAlert("Passwords do not match. Please re-enter identical passwords.", "error");
        return false;
    }

    if (agreeTerms && !agreeTerms.checked) {
        showAlert("Please agree to the Farm Safety & Management Terms to continue.", "error");
        return false;
    }

    if (regSubmitBtn) {
        regSubmitBtn.disabled = true;
        regSubmitBtn.innerHTML = "<span>🌱 Creating Account...</span>";
    }

    fetch("register", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ fullName, username, email, farmType, password })
    })
        .then(response => response.json())
        .then(result => {
            if (!result.success) {
                throw new Error(result.message || "Registration failed.");
            }

            // Sync with local storage user cache
            const users = getStoredUsers();
            if (!users.some(u => u.username.toLowerCase() === username.toLowerCase())) {
                users.push({ username, password, role: "USER", fullName, email, farmType });
                saveUsers(users);
            }

            showAlert("✨ Account created successfully! Redirecting to login...", "success");
            setTimeout(() => {
                window.location.href = "login.html";
            }, 800);
        })
        .catch(error => {
            const isOffline = window.location.protocol === "file:" || error.message.includes("Failed to fetch");
            if (isOffline) {
                const users = getStoredUsers();
                users.push({ username, password, role: "USER", fullName, email, farmType });
                saveUsers(users);
                showAlert("✨ Account created locally! Redirecting to login...", "success");
                setTimeout(() => {
                    window.location.href = "login.html";
                }, 800);
                return;
            }
            showAlert(error.message || "Unable to contact the server.", "error");
            if (regSubmitBtn) {
                regSubmitBtn.disabled = false;
                regSubmitBtn.innerHTML = "<span>🌱 Create Farm Account</span>";
            }
        });

    return false;
}

// ================= BACKEND DATABASE SYNC =================

async function loadServerData() {
    try {
        const response = await fetch("api/data");
        if (response.ok) {
            const data = await response.json();
            if (Array.isArray(data.animals) && data.animals.length > 0) saveAnimals(data.animals);
            if (Array.isArray(data.feed) && data.feed.length > 0) saveFeed(data.feed);
            if (Array.isArray(data.health) && data.health.length > 0) saveHealth(data.health);
            if (Array.isArray(data.users) && data.users.length > 0) saveUsers(data.users);
            refreshAllDashboardViews();
            return;
        }
    } catch (e) {
        console.warn("Could not load /api/data, attempting /api/users fallback:", e);
    }

    try {
        const userRes = await fetch("api/users");
        if (userRes.ok) {
            const users = await userRes.json();
            if (Array.isArray(users) && users.length > 0) {
                saveUsers(users);
                refreshAllDashboardViews();
            }
        }
    } catch (e) {
        // Offline mode
    }
}

// Reset data to initial sample values
function resetToSampleData() {
    if (confirm("Reset all farm data back to sample initial state?")) {
        saveAnimals(SAMPLE_ANIMALS);
        saveFeed(SAMPLE_FEED);
        saveHealth(SAMPLE_HEALTH);
        refreshAllDashboardViews();
        showAlert("✨ Demo farm records reloaded successfully!", "success");
    }
}

// Modal helpers
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add("open");
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove("open");
}

// Tab Switching
function switchDashTab(tabName) {
    const tabs = ["animals", "feed", "health", "users"];
    tabs.forEach(t => {
        const btn = document.getElementById("tabBtn" + t.charAt(0).toUpperCase() + t.slice(1));
        const panel = document.getElementById("panel" + t.charAt(0).toUpperCase() + t.slice(1));
        if (btn) btn.classList.remove("active");
        if (panel) panel.classList.remove("active");
    });

    const activeBtn = document.getElementById("tabBtn" + tabName.charAt(0).toUpperCase() + tabName.slice(1));
    const activePanel = document.getElementById("panel" + tabName.charAt(0).toUpperCase() + tabName.slice(1));
    if (activeBtn) activeBtn.classList.add("active");
    if (activePanel) activePanel.classList.add("active");
}

// ================= RENDER ANIMALS =================

function renderAnimalsTable(query = "", speciesFilter = "") {
    const tbody = document.getElementById("animalsTableBody");
    if (!tbody) return;

    let animals = getStoredAnimals();
    const q = query.toLowerCase().trim();

    if (q) {
        animals = animals.filter(a =>
            (a.tag && a.tag.toLowerCase().includes(q)) ||
            (a.name && a.name.toLowerCase().includes(q)) ||
            (a.species && a.species.toLowerCase().includes(q)) ||
            (a.breed && a.breed.toLowerCase().includes(q))
        );
    }

    if (speciesFilter) {
        animals = animals.filter(a => a.species && a.species.toLowerCase() === speciesFilter.toLowerCase());
    }

    if (animals.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="empty-state">
            <div class="empty-state-icon">🐄</div>
            <p>No animals found matching your search criteria.</p>
        </td></tr>`;
        return;
    }

    tbody.innerHTML = animals.map(a => {
        let badgeClass = "badge-healthy";
        if (a.health === "Under Treatment") badgeClass = "badge-treatment";
        if (a.health === "Quarantine") badgeClass = "badge-quarantine";

        return `<tr>
            <td><strong>${a.tag}</strong></td>
            <td>${a.name}</td>
            <td>${a.species}</td>
            <td>${a.breed || '—'}</td>
            <td>${a.age || '—'}</td>
            <td>${a.gender}</td>
            <td><span class="badge ${badgeClass}">${a.health}</span></td>
            <td style="text-align: right;">
                <button type="button" class="btn-table-action btn-del" onclick="deleteAnimal('${a.tag}')">🗑️ Delete</button>
            </td>
        </tr>`;
    }).join("");
}

function filterAnimalsTable() {
    const q = document.getElementById("animalSearchInput") ? document.getElementById("animalSearchInput").value : "";
    const s = document.getElementById("animalSpeciesFilter") ? document.getElementById("animalSpeciesFilter").value : "";
    renderAnimalsTable(q, s);
}

function handleAddAnimal(event) {
    event.preventDefault();
    const tag = document.getElementById("newAnimalTag").value.trim().toUpperCase();
    const name = document.getElementById("newAnimalName").value.trim();
    const species = document.getElementById("newAnimalSpecies").value;
    const breed = document.getElementById("newAnimalBreed").value.trim();
    const age = document.getElementById("newAnimalAge").value.trim();
    const gender = document.getElementById("newAnimalGender").value;
    const health = document.getElementById("newAnimalHealth").value;

    const animals = getStoredAnimals();
    if (animals.some(a => a.tag && a.tag.toUpperCase() === tag)) {
        alert("An animal with Tag ID " + tag + " already exists!");
        return false;
    }

    const newAnimal = { tag, name, species, breed, age, gender, health };
    animals.unshift(newAnimal);
    saveAnimals(animals);
    closeModal("addAnimalModal");
    event.target.reset();
    refreshAllDashboardViews();
    showAlert(`✨ Animal <strong>${name} (${tag})</strong> added to livestock records!`, "success");

    // Persist to database API
    fetch("api/animals", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams(newAnimal)
    }).catch(e => console.warn("Database sync error:", e));

    return false;
}

function deleteAnimal(tag) {
    if (confirm("Are you sure you want to delete animal with Tag " + tag + "?")) {
        let animals = getStoredAnimals();
        animals = animals.filter(a => a.tag !== tag);
        saveAnimals(animals);
        refreshAllDashboardViews();
        showAlert(`Animal record <strong>${tag}</strong> removed.`, "info");

        // Delete from database API
        fetch("api/animals?tag=" + encodeURIComponent(tag), { method: "DELETE" })
            .catch(e => console.warn("Database sync error:", e));
    }
}

// ================= RENDER FEED =================

function renderFeedTable(query = "") {
    const tbody = document.getElementById("feedTableBody");
    if (!tbody) return;

    let feeds = getStoredFeed();
    const q = query.toLowerCase().trim();

    if (q) {
        feeds = feeds.filter(f => (f.name && f.name.toLowerCase().includes(q)) || (f.code && f.code.toLowerCase().includes(q)));
    }

    if (feeds.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state">
            <div class="empty-state-icon">🌾</div>
            <p>No feed stock items found.</p>
        </td></tr>`;
        return;
    }

    tbody.innerHTML = feeds.map(f => {
        const isLow = f.stock <= 100;
        const statusBadge = isLow
            ? '<span class="badge badge-low">⚠️ Low Stock</span>'
            : '<span class="badge badge-adequate">✅ Adequate</span>';

        return `<tr>
            <td><strong>${f.code}</strong></td>
            <td>${f.name}</td>
            <td><strong>${f.stock} kg</strong></td>
            <td>${f.consumption} kg/day</td>
            <td>${statusBadge}</td>
            <td>${f.updated}</td>
            <td style="text-align: right;">
                <button type="button" class="btn-table-action btn-restock" onclick="openRestockModal('${f.code}')">📦 Restock</button>
                <button type="button" class="btn-table-action btn-del" onclick="deleteFeed('${f.code}')">🗑️</button>
            </td>
        </tr>`;
    }).join("");
}

function filterFeedTable() {
    const q = document.getElementById("feedSearchInput") ? document.getElementById("feedSearchInput").value : "";
    renderFeedTable(q);
}

function handleAddFeed(event) {
    event.preventDefault();
    const name = document.getElementById("newFeedName").value.trim();
    const stock = parseFloat(document.getElementById("newFeedStock").value);
    const consumption = parseFloat(document.getElementById("newFeedConsumption").value);

    const feeds = getStoredFeed();
    const code = "FD-0" + (feeds.length + 1);
    const today = new Date().toISOString().split("T")[0];

    const newFeed = { code, name, stock, consumption, updated: today };
    feeds.push(newFeed);
    saveFeed(feeds);
    closeModal("addFeedModal");
    event.target.reset();
    refreshAllDashboardViews();
    showAlert(`🌾 Feed item <strong>${name}</strong> added to stock!`, "success");

    // Persist to database API
    fetch("api/feed", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ code, name, stock, consumption })
    }).catch(e => console.warn("Database sync error:", e));

    return false;
}

function openRestockModal(code) {
    const feeds = getStoredFeed();
    const feed = feeds.find(f => f.code === code);
    if (!feed) return;

    document.getElementById("restockFeedId").value = code;
    document.getElementById("restockFeedLabel").textContent = `Restock: ${feed.name} (Current: ${feed.stock} kg)`;
    document.getElementById("restockAmount").value = "";
    openModal("restockFeedModal");
}

function handleRestockFeed(event) {
    event.preventDefault();
    const code = document.getElementById("restockFeedId").value;
    const amount = parseFloat(document.getElementById("restockAmount").value);

    if (isNaN(amount) || amount <= 0) return false;

    const feeds = getStoredFeed();
    const feed = feeds.find(f => f.code === code);
    if (feed) {
        feed.stock += amount;
        feed.updated = new Date().toISOString().split("T")[0];
        saveFeed(feeds);
        closeModal("restockFeedModal");
        refreshAllDashboardViews();
        showAlert(`📦 Added <strong>${amount} kg</strong> to ${feed.name}!`, "success");

        // Persist restock to database API
        fetch("api/feed/restock", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({ code, amount })
        }).catch(e => console.warn("Database sync error:", e));
    }
    return false;
}

function deleteFeed(code) {
    if (confirm("Delete feed record " + code + "?")) {
        let feeds = getStoredFeed();
        feeds = feeds.filter(f => f.code !== code);
        saveFeed(feeds);
        refreshAllDashboardViews();
        showAlert(`Feed record removed.`, "info");

        // Delete from database API
        fetch("api/feed?code=" + encodeURIComponent(code), { method: "DELETE" })
            .catch(e => console.warn("Database sync error:", e));
    }
}

// ================= RENDER HEALTH =================

function renderHealthTable(query = "") {
    const tbody = document.getElementById("healthTableBody");
    if (!tbody) return;

    let health = getStoredHealth();
    const q = query.toLowerCase().trim();

    if (q) {
        health = health.filter(h =>
            (h.id && h.id.toLowerCase().includes(q)) ||
            (h.animal && h.animal.toLowerCase().includes(q)) ||
            (h.type && h.type.toLowerCase().includes(q)) ||
            (h.vet && h.vet.toLowerCase().includes(q))
        );
    }

    if (health.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="empty-state">
            <div class="empty-state-icon">🩺</div>
            <p>No health or medical records found.</p>
        </td></tr>`;
        return;
    }

    tbody.innerHTML = health.map(h => {
        const badgeClass = h.status === "Completed" ? "badge-completed" : "badge-pending";
        return `<tr>
            <td><strong>${h.id}</strong></td>
            <td>${h.animal}</td>
            <td>${h.type}</td>
            <td>${h.vet}</td>
            <td>${h.date}</td>
            <td><span class="badge ${badgeClass}">${h.status}</span></td>
            <td>${h.notes || '—'}</td>
            <td style="text-align: right;">
                <button type="button" class="btn-table-action btn-del" onclick="deleteHealth('${h.id}')">🗑️</button>
            </td>
        </tr>`;
    }).join("");
}

function filterHealthTable() {
    const q = document.getElementById("healthSearchInput") ? document.getElementById("healthSearchInput").value : "";
    renderHealthTable(q);
}

function handleAddHealth(event) {
    event.preventDefault();
    const animal = document.getElementById("newHealthAnimal").value.trim();
    const type = document.getElementById("newHealthType").value.trim();
    const vet = document.getElementById("newHealthVet").value.trim();
    const date = document.getElementById("newHealthDate").value;
    const status = document.getElementById("newHealthStatus").value;
    const notes = document.getElementById("newHealthNotes").value.trim();

    const health = getStoredHealth();
    const id = "HL-" + (Math.floor(Math.random() * 800) + 200);

    const newHealth = { id, animal, type, vet, date, status, notes };
    health.unshift(newHealth);
    saveHealth(health);
    closeModal("addHealthModal");
    event.target.reset();
    refreshAllDashboardViews();
    showAlert(`🩺 Medical log <strong>${id}</strong> saved!`, "success");

    // Persist to database API
    fetch("api/health", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams(newHealth)
    }).catch(e => console.warn("Database sync error:", e));

    return false;
}

function deleteHealth(id) {
    if (confirm("Delete health record " + id + "?")) {
        let health = getStoredHealth();
        health = health.filter(h => h.id !== id);
        saveHealth(health);
        refreshAllDashboardViews();
        showAlert(`Medical log removed.`, "info");

        // Delete from database API
        fetch("api/health?id=" + encodeURIComponent(id), { method: "DELETE" })
            .catch(e => console.warn("Database sync error:", e));
    }
}

// ================= RENDER USERS =================

function renderUsersTable() {
    const tbody = document.getElementById("usersTableBody");
    if (!tbody) return;

    const users = getStoredUsers();
    tbody.innerHTML = users.map(u => {
        const isAdmin = u.role === "ADMIN" || u.username === "admin";
        const rolePill = isAdmin
            ? '<span class="role-chip admin" style="font-size: 11px; padding: 2px 8px;">ADMIN</span>'
            : '<span class="role-chip user" style="font-size: 11px; padding: 2px 8px;">USER</span>';

        return `<tr>
            <td><strong>${u.fullName || u.username}</strong></td>
            <td>${u.username}</td>
            <td>${u.email || '—'}</td>
            <td>${u.farmType || 'General Farm'}</td>
            <td>${rolePill}</td>
            <td style="text-align: right;">
                ${u.username === 'admin'
                    ? '<span style="font-size: 12px; color: #94a3b8;">System Master</span>'
                    : `<button type="button" class="btn-table-action btn-del" onclick="deleteUser('${u.username}')">Remove</button>`}
            </td>
        </tr>`;
    }).join("");
}

function deleteUser(username) {
    if (confirm("Remove user account '" + username + "' from system?")) {
        let users = getStoredUsers();
        users = users.filter(u => u.username !== username);
        saveUsers(users);
        refreshAllDashboardViews();
        showAlert(`User account <strong>${username}</strong> removed.`, "info");

        // Delete from database API
        fetch("api/users?username=" + encodeURIComponent(username), { method: "DELETE" })
            .then(res => res.json())
            .then(() => loadServerData())
            .catch(e => console.warn("Database sync error:", e));
    }
}

// ================= REFRESH ALL DASHBOARD STATS & VIEWS =================

function refreshAllDashboardViews() {
    const animals = getStoredAnimals();
    const feeds = getStoredFeed();
    const health = getStoredHealth();
    const users = getStoredUsers();

    // Update KPI summary metrics
    const statAnimals = document.getElementById("statTotalAnimals");
    if (statAnimals) statAnimals.textContent = animals.length;

    const statFeed = document.getElementById("statTotalFeed");
    if (statFeed) {
        const totalKg = feeds.reduce((sum, f) => sum + (parseFloat(f.stock) || 0), 0);
        statFeed.textContent = Math.round(totalKg) + " kg";
    }

    const statHealth = document.getElementById("statHealthLogs");
    if (statHealth) statHealth.textContent = health.length;

    const statUsers = document.getElementById("statTotalUsers");
    if (statUsers) statUsers.textContent = users.length;

    // Render tables
    renderAnimalsTable();
    renderFeedTable();
    renderHealthTable();
    renderUsersTable();
}

// ================= INITIALIZATION ON DOM LOAD =================

document.addEventListener("DOMContentLoaded", () => {
    let session = getCurrentSession();
    if (!session && window.location.pathname.includes("dashboard")) {
        session = { username: "Administrator", role: "ADMIN" };
        setCurrentSession("Administrator", "ADMIN");
    }

    const userGreeting = document.getElementById("userGreeting");
    const roleBadge = document.getElementById("roleBadge");
    const dashboardSubtitle = document.getElementById("dashboardSubtitle");

    if (session) {
        if (userGreeting) userGreeting.textContent = session.username;
        if (roleBadge) {
            if (session.role === "ADMIN") {
                roleBadge.className = "role-chip admin";
                roleBadge.innerHTML = "🛡️ Admin Portal";
            } else {
                roleBadge.className = "role-chip user";
                roleBadge.innerHTML = "🌾 Farmer";
            }
        }
        if (dashboardSubtitle && session.role === "ADMIN") {
            dashboardSubtitle.textContent = "Farm Administrative Control & Data Console";
        }
    }

    // Initialize all tables & stats if on dashboard
    if (document.getElementById("animalsTableBody")) {
        refreshAllDashboardViews();
        // Sync with backend database
        loadServerData();
    }
});
