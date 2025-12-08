<script>
  let { data } = $props();
  let isAuthenticated = $state(data.isAuthenticated);
  let user = $state(data.user);

  $effect(() => {
    isAuthenticated = data.isAuthenticated;
    user = data.user;
  });
</script>

<!-- Hero Section with Video -->
<div class="hero-section">
  <video autoplay muted loop playsinline class="hero-video">
    <source src="/hero-video.mp4" type="video/mp4" />
  </video>
  <div class="hero-overlay"></div>
  <div class="hero-content">
    <h1 class="display-3 fw-bold text-white">ShareWay</h1>
    <p class="lead text-white mb-4">Share the ride, share the way.</p>
    
    {#if isAuthenticated}
      <div class="d-flex gap-3 justify-content-center flex-wrap">
        <a href="/rides" class="btn btn-light btn-lg">Browse Rides</a>
        <a href="/driver" class="btn btn-outline-light btn-lg">Driver Dashboard</a>
      </div>
    {:else}
      <div class="d-flex gap-3 justify-content-center flex-wrap">
        <a href="/login" class="btn btn-light btn-lg">Login</a>
        <a href="/signup" class="btn btn-outline-light btn-lg">Sign Up</a>
      </div>
    {/if}
  </div>
</div>

<!-- Features Section -->
<div class="container py-5">
  <div class="text-center mb-5">
    <h2 class="fw-bold">How it works</h2>
    <p class="text-muted">Join thousands of people sharing rides across Switzerland</p>
  </div>
  
  <div class="row g-4">
    <div class="col-md-4 text-center">
      <div class="mb-3">
        <i class="bi bi-car-front fs-1 text-dark"></i>
      </div>
      <h4>Find a Ride</h4>
      <p class="text-muted">Search for available rides to your destination and book a seat.</p>
    </div>
    <div class="col-md-4 text-center">
      <div class="mb-3">
        <i class="bi bi-person-badge fs-1 text-dark"></i>
      </div>
      <h4>Become a Driver</h4>
      <p class="text-muted">Get verified, add your vehicle, and offer rides to earn money.</p>
    </div>
    <div class="col-md-4 text-center">
      <div class="mb-3">
        <i class="bi bi-star fs-1 text-dark"></i>
      </div>
      <h4>Rate & Review</h4>
      <p class="text-muted">Share your experience and help build a trusted community.</p>
    </div>
  </div>
</div>

<!-- Stats Section -->
<div class="bg-light py-5">
  <div class="container">
    <div class="row text-center">
      <div class="col-md-4 mb-3 mb-md-0">
        <h2 class="fw-bold text-primary">500+</h2>
        <p class="text-muted mb-0">Rides Shared</p>
      </div>
      <div class="col-md-4 mb-3 mb-md-0">
        <h2 class="fw-bold text-primary">200+</h2>
        <p class="text-muted mb-0">Verified Drivers</p>
      </div>
      <div class="col-md-4">
        <h2 class="fw-bold text-primary">50+</h2>
        <p class="text-muted mb-0">Swiss Cities</p>
      </div>
    </div>
  </div>
</div>

<!-- CTA Section -->
<div class="container py-5 text-center">
  <h2 class="fw-bold mb-3">Ready to start sharing?</h2>
  <p class="text-muted mb-4">Join ShareWay today and make your commute greener and cheaper.</p>
  {#if !isAuthenticated}
    <a href="/signup" class="btn btn-primary btn-lg">Get Started</a>
  {:else}
    <a href="/rides" class="btn btn-primary btn-lg">Find a Ride</a>
  {/if}
</div>

<style>
  .hero-section {
    position: relative;
    height: 80vh;
    min-height: 500px;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: calc(-50vw + 50%);
    margin-right: calc(-50vw + 50%);
    width: 100vw;
  }

  .hero-video {
    position: absolute;
    top: 50%;
    left: 50%;
    min-width: 100%;
    min-height: 100%;
    transform: translate(-50%, -50%);
    object-fit: cover;
    z-index: -2;
  }

  .hero-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    z-index: -1;
  }

  .hero-content {
    text-align: center;
    z-index: 1;
    padding: 2rem;
  }
</style>