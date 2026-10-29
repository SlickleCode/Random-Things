const COLLAPSED_STORAGE_KEY_PREFIX = "sidebar-collapsed-";
const SCROLL_STORAGE_KEY = "sidebar-scroll-position";

window.onload = function() {
    document.getElementById("open-sidebar").addEventListener("click", openSidebar);
    document.getElementById("close-sidebar").addEventListener("click", closeSidebar);
    document.getElementById("search-field").addEventListener("input", filterSidebar);
    window.onresize = removeMobileSidebarClasses;
    initSidebarCategories();
    initSidebarScrollPosition();
    initThemeToggle();
}

function initThemeToggle() {
    document.getElementById("theme-toggle").addEventListener("click", function() {
        let current = document.documentElement.getAttribute("data-theme");
        setTheme(current === "dark" ? "light" : "dark");
    });
}

function setTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);
    try {
        localStorage.setItem("theme", theme);
    } catch (e) {
        // localStorage unavailable (private browsing, etc.) - theme just won't persist
    }
}

function initSidebarScrollPosition() {
    let sidebarList = document.getElementById("sidebar-item-list");
    try {
        let scrollTop = sessionStorage.getItem(SCROLL_STORAGE_KEY);
        if (scrollTop !== null) {
            sidebarList.scrollTop = parseInt(scrollTop, 10);
        }
    } catch (e) {
        // sessionStorage unavailable (private browsing, etc.) - scroll position just won't persist
    }
    sidebarList.addEventListener("scroll", function() {
        try {
            sessionStorage.setItem(SCROLL_STORAGE_KEY, sidebarList.scrollTop);
        } catch (e) {
            // sessionStorage unavailable (private browsing, etc.) - scroll position just won't persist
        }
    });
}

function initSidebarCategories() {
    let categories = document.getElementsByClassName("sidebar-category");
    for (let category of categories) {
        let header = category.querySelector(".sidebar-header-item");
        if (isCategoryCollapsedByDefault(category)) {
            setCategoryCollapsed(category, header, true);
        }
        header.addEventListener("click", function() {
            setCategoryCollapsed(category, header, !category.classList.contains("collapsed"));
        });
    }
}

function isCategoryCollapsedByDefault(category) {
    try {
        return localStorage.getItem(COLLAPSED_STORAGE_KEY_PREFIX + category.dataset.category) === "true";
    } catch (e) {
        return false;
    }
}

function setCategoryCollapsed(category, header, collapsed) {
    category.classList.toggle("collapsed", collapsed);
    header.setAttribute("aria-expanded", String(!collapsed));
    try {
        localStorage.setItem(COLLAPSED_STORAGE_KEY_PREFIX + category.dataset.category, String(collapsed));
    } catch (e) {
        // localStorage unavailable (private browsing, etc.) - collapse state just won't persist
    }
}

function filterSidebar(event) {
    let query = event.target.value.trim().toLowerCase();
    let sidebarList = document.getElementById("sidebar-item-list");
    sidebarList.classList.toggle("searching", query !== "");

    let categories = sidebarList.getElementsByClassName("sidebar-category");
    for (let category of categories) {
        let items = category.getElementsByClassName("sidebar-line-item");
        let visibleCount = 0;
        for (let item of items) {
            let matches = query === "" || item.dataset.keywords.includes(query);
            item.classList.toggle("search-hidden", !matches);
            if (matches) visibleCount++;
        }
        category.classList.toggle("no-match", visibleCount === 0);
    }
}

function openSidebar() {
    let sidebar = document.getElementsByClassName("sidebar-component")[0];
    sidebar.classList.add("sidebar-component-slide-in");
    sidebar.classList.remove("sidebar-component-slide-out");
}

function closeSidebar() {
    let sidebar = document.getElementsByClassName("sidebar-component")[0];
    sidebar.classList.add("sidebar-component-slide-out");
    sidebar.classList.remove("sidebar-component-slide-in");
}

function removeMobileSidebarClasses() {
    let sidebar = document.getElementsByClassName("sidebar-component")[0];
    sidebar.classList.remove("sidebar-component-slide-out");
    sidebar.classList.remove("sidebar-component-slide-in");
}

function openModalWithPicture(image) {
    let modalPicture = document.getElementById("modal-picture")
    modalPicture.src = image.src;
    document.getElementById("modal").addEventListener("click", hideModal);
    displayModal();
}

function displayModal() {
    let modal = document.getElementById("modal");
    modal.style.display = "flex";
}

function hideModal() {
    let modal = document.getElementById("modal");
    modal.style.display = "none";
    modal.removeEventListener("click", hideModal);
}