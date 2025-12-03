<script>
  let { data } = $props();
  let { user, isAuthenticated } = data;
</script>

<div class="container mt-5">
  <div class="row">
    <div class="col-md-8 offset-md-2 text-center">
      <h1 class="display-4">Welcome to ShareWay</h1>
      <p class="lead">Share rides, save money, reduce emissions.</p>
      
      {#if isAuthenticated}
        <p>Hello, <strong>{user.name}</strong>!</p>
        <div class="d-grid gap-2 d-md-flex justify-content-md-center mt-4">
          <a href="/rides" class="btn btn-primary btn-lg">Browse Rides</a>
          {#if user.user_roles && (user.user_roles.includes("driver") || user.user_roles.includes("admin"))}
            <a href="/rides/create" class="btn btn-outline-primary btn-lg">Offer a Ride</a>
          {/if}
        </div>
      {:else}
        <p class="mt-4">Join our community of riders and drivers.</p>
        <div class="d-grid gap-2 d-md-flex justify-content-md-center mt-4">
          <a href="/login" class="btn btn-primary btn-lg">Login</a>
          <a href="/signup" class="btn btn-outline-primary btn-lg">Sign Up</a>
        </div>
      {/if}
    </div>
  </div>
</div>