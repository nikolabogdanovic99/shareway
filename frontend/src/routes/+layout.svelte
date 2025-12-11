<script>
  import favicon from "$lib/assets/favicon.svg";
  import "./styles.css";
  let { data, children } = $props();
  let { user, isAuthenticated } = data;
</script>

<svelte:head>
  <link rel="icon" href={favicon} />
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link
    href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"
    rel="stylesheet"
  />
</svelte:head>

<nav class="navbar navbar-expand-lg navbar-shareway">
  <div class="container">
    <a class="navbar-brand" href="/">
      <i class="bi bi-car-front-fill"></i>
      ShareWay
    </a>
    <button
      class="navbar-toggler"
      type="button"
      data-bs-toggle="collapse"
      data-bs-target="#navbarNav"
      aria-controls="navbarNav"
      aria-expanded="false"
      aria-label="Toggle navigation"
    >
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNav">
      <ul class="navbar-nav me-auto mb-2 mb-lg-0">
        {#if isAuthenticated}
          <li class="nav-item">
            <a class="nav-link" href="/rides">
              <i class="bi bi-search me-1"></i>
              Find Rides
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="/bookings">
              <i class="bi bi-ticket-perforated me-1"></i>
              My Bookings
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="/driver">
              <i class="bi bi-speedometer2 me-1"></i>
              Driver Dashboard
            </a>
          </li>
          {#if user.user_roles && user.user_roles.includes("admin")}
            <li class="nav-item">
              <a class="nav-link" href="/admin/verifications">
                <i class="bi bi-shield-check me-1"></i>
                Verifications
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/admin/moderation">
                <i class="bi bi-exclamation-triangle me-1"></i>
                Moderation
              </a>
            </li>
          {/if}
        {/if}
      </ul>
      <div class="d-flex align-items-center gap-2">
        {#if isAuthenticated}
          <a href="/account" class="nav-link d-flex align-items-center gap-2">
            <div
              class="avatar"
              style="width: 32px; height: 32px; font-size: 0.875rem;"
            >
              {user.name?.charAt(0) || "U"}
            </div>
            <span class="d-none d-lg-inline">{user.name}</span>
          </a>
          <form method="POST" action="/logout" style="display: inline;">
            <button type="submit" class="btn btn-outline-light btn-sm">
              <i class="bi bi-box-arrow-right me-1"></i>
              Logout
            </button>
          </form>
        {:else}
          <a href="/login" class="btn btn-primary">
            <i class="bi bi-box-arrow-in-right me-1"></i>
            Login
          </a>
          <a href="/signup" class="btn btn-outline-light"> Sign Up </a>
        {/if}
      </div>
    </div>
  </div>
</nav>

<main class="container py-4">
  {@render children()}
</main>

<footer class="footer-shareway">
  <div class="container">
    <div class="row">
      <div class="col-md-4 mb-3 mb-md-0">
        <h5 class="d-flex align-items-center gap-2">
          <i class="bi bi-car-front-fill" style="color: var(--sw-primary);"></i>
          ShareWay
        </h5>
        <p class="text-white-50 mb-0">
          Share rides, save money, help the environment.
        </p>
      </div>
      <div class="col-md-4 mb-3 mb-md-0">
        <h6>Quick Links</h6>
        <ul class="list-unstyled mb-0">
          <li>
            <a href="/rides"
              ><i class="bi bi-chevron-right me-1"></i>Find Rides</a
            >
          </li>
          <li>
            <a href="/driver"
              ><i class="bi bi-chevron-right me-1"></i>Offer a Ride</a
            >
          </li>
          <li>
            <a href="/account"
              ><i class="bi bi-chevron-right me-1"></i>My Account</a
            >
          </li>
        </ul>
      </div>
      <div class="col-md-4">
        <h6>Contact</h6>
        <ul class="list-unstyled mb-0">
          <li><i class="bi bi-envelope me-2"></i>support@shareway.ch</li>
          <li><i class="bi bi-geo-alt me-2"></i>Zürich, Switzerland</li>
        </ul>
      </div>
    </div>
    <hr class="my-3" style="border-color: rgba(255,255,255,0.1);" />
    <div class="text-center text-white-50">
      <small>© 2025 ShareWay. All rights reserved.</small>
    </div>
  </div>
</footer>
