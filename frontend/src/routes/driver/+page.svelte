<script>
  let { data } = $props();
  
  let dbUser = $state(data.dbUser);
  let user = $state(data.user);
  let isAuthenticated = $state(data.isAuthenticated);
  let ridesCount = $state(data.ridesCount || 0);
  let pendingCount = $state(data.pendingCount || 0);
  let vehiclesCount = $state(data.vehiclesCount || 0);

  const isDriverOrAdmin = $derived(
    isAuthenticated &&
      user?.user_roles &&
      (user.user_roles.includes("user") || user.user_roles.includes("admin"))
  );

  const isAdmin = $derived(
    isAuthenticated && user?.user_roles?.includes("admin")
  );
  const isProfileComplete = $derived(dbUser?.firstName && dbUser?.lastName);
  const isVerified = $derived(
    isAdmin || dbUser?.verificationStatus === "VERIFIED"
  );
  const verificationStatus = $derived(
    dbUser?.verificationStatus || "UNVERIFIED"
  );
</script>

{#if !isDriverOrAdmin}
  <div class="alert alert-warning mt-3">Only drivers can access this page.</div>
{:else if !isProfileComplete}
  <div class="alert alert-warning mt-3">
    <strong>Profile incomplete!</strong>
    Please <a href="/account">complete your profile</a> first.
  </div>
{:else if !isVerified}
  <!-- Verification Required Screen -->
  <div class="text-center py-5">
    <h1 class="mb-4">Driver Dashboard</h1>
    
    <div class="card mx-auto" style="max-width: 500px;">
      <div class="card-body py-5">
        {#if verificationStatus === "UNVERIFIED"}
          <i class="bi bi-shield-exclamation text-warning" style="font-size: 4rem;"></i>
          <h3 class="mt-3">Verification Required</h3>
          <p class="text-muted mb-4">
            To access the Driver Dashboard, you need to verify your identity first.
          </p>
          <a href="/account" class="btn btn-primary btn-lg">
            <i class="bi bi-shield-check me-2"></i>Start Verification
          </a>
        {:else if verificationStatus === "PENDING"}
          <i class="bi bi-hourglass-split text-info" style="font-size: 4rem;"></i>
          <h3 class="mt-3">Verification Pending</h3>
          <p class="text-muted mb-4">
            Your verification is being reviewed. This usually takes 1-2 business days.
          </p>
          <a href="/account" class="btn btn-outline-secondary">
            View Status
          </a>
        {:else if verificationStatus === "DENIED"}
          <i class="bi bi-x-circle text-danger" style="font-size: 4rem;"></i>
          <h3 class="mt-3">Verification Denied</h3>
          <p class="text-muted mb-4">
            Your verification was denied. Please upload new documents.
          </p>
          <a href="/account" class="btn btn-primary btn-lg">
            Try Again
          </a>
        {/if}
      </div>
    </div>
  </div>
{:else}
  <!-- Dashboard -->
  <div class="text-center py-4">
    <h1 class="mb-2">Driver Dashboard</h1>
    <p class="text-muted">Manage your rides, bookings and vehicles</p>
  </div>

  <div class="row justify-content-center g-4 mt-2">
    <!-- My Rides Card -->
    <div class="col-md-4">
      <a href="/driver/rides" class="text-decoration-none">
        <div class="card h-100 text-center hover-lift" style="cursor: pointer;">
          <div class="card-body py-5">
            <i class="bi bi-car-front text-primary" style="font-size: 3rem;"></i>
            <h4 class="mt-3 text-dark">My Rides</h4>
            <p class="text-muted mb-0">Create and manage your rides</p>
            <div class="mt-3">
              <span class="badge bg-primary" style="font-size: 1.2rem;">{ridesCount}</span>
            </div>
          </div>
        </div>
      </a>
    </div>

    <!-- Booking Requests Card -->
    <div class="col-md-4">
      <a href="/driver/requests" class="text-decoration-none">
        <div class="card h-100 text-center hover-lift" style="cursor: pointer;">
          <div class="card-body py-5">
            <i class="bi bi-envelope text-warning" style="font-size: 3rem;"></i>
            <h4 class="mt-3 text-dark">Booking Requests</h4>
            <p class="text-muted mb-0">Approve or reject requests</p>
            <div class="mt-3">
              {#if pendingCount > 0}
                <span class="badge bg-warning text-dark" style="font-size: 1.2rem;">{pendingCount} new</span>
              {:else}
                <span class="badge bg-secondary" style="font-size: 1.2rem;">0</span>
              {/if}
            </div>
          </div>
        </div>
      </a>
    </div>

    <!-- Vehicles Card -->
    <div class="col-md-4">
      <a href="/driver/vehicles" class="text-decoration-none">
        <div class="card h-100 text-center hover-lift" style="cursor: pointer;">
          <div class="card-body py-5">
            <i class="bi bi-truck text-success" style="font-size: 3rem;"></i>
            <h4 class="mt-3 text-dark">My Vehicles</h4>
            <p class="text-muted mb-0">Manage your vehicles</p>
            <div class="mt-3">
              <span class="badge bg-success" style="font-size: 1.2rem;">{vehiclesCount}</span>
            </div>
          </div>
        </div>
      </a>
    </div>
  </div>
{/if}

<style>
  .hover-lift {
    transition: all 0.3s ease;
  }
  .hover-lift:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 40px -10px rgba(0, 0, 0, 0.15);
  }
</style>