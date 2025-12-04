<script>
  let { data } = $props();
  let isAuthenticated = $state(data.isAuthenticated);
  let user = $state(data.user);

  $effect(() => {
    isAuthenticated = data.isAuthenticated;
    user = data.user;
  });
</script>

<div class="container mt-5">
  <div class="text-center">
    <h1 class="display-4">Welcome to ShareWay</h1>
    <p class="lead text-muted">Share rides, save money, reduce emissions.</p>
    
    {#if isAuthenticated}
      <div class="mt-4">
        <p>Hello, {user?.name || 'User'}!</p>
        <a href="/rides" class="btn btn-primary btn-lg me-2">Browse Rides</a>
        <a href="/driver" class="btn btn-outline-primary btn-lg">Driver Dashboard</a>
      </div>
    {:else}
      <div class="mt-4">
        <a href="/login" class="btn btn-primary btn-lg me-2">Login</a>
        <a href="/signup" class="btn btn-outline-primary btn-lg">Sign Up</a>
      </div>
    {/if}
  </div>

  <div class="row mt-5">
    <div class="col-md-4 text-center">
      <h3>🚗 Find a Ride</h3>
      <p>Search for available rides to your destination and book a seat.</p>
    </div>
    <div class="col-md-4 text-center">
      <h3>🪪 Become a Driver</h3>
      <p>Get verified, add your vehicle, and offer rides to earn money.</p>
    </div>
    <div class="col-md-4 text-center">
      <h3>⭐ Rate & Review</h3>
      <p>Share your experience and help build a trusted community.</p>
    </div>
  </div>
</div>