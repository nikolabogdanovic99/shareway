<script>
  let { data } = $props();

  let bookings = $state(data.bookings || []);
  let rides = $state(data.rides || []);
  let users = $state(data.users || []);

  $effect(() => {
    bookings = data.bookings || [];
    rides = data.rides || [];
    users = data.users || [];
  });

  // Get ride details
  function getRide(rideId) {
    return rides.find(r => r.id === rideId);
  }

  // Get driver name
  function getDriverName(driverId) {
    if (!users || users.length === 0) return "Unknown";
    const driver = users.find(u => u.email === driverId);
    if (!driver) return "Unknown";
    if (driver.firstName && driver.lastName) {
      return `${driver.firstName} ${driver.lastName}`;
    }
    return driver.name || "Unknown";
  }

  // Format date
  function formatDate(dateString) {
    if (!dateString) return "-";
    const date = new Date(dateString);
    return (
      date.toLocaleDateString("de-CH") +
      " " +
      date.toLocaleTimeString("de-CH", { hour: "2-digit", minute: "2-digit" })
    );
  }

  // Get status badge class
  function getStatusClass(status) {
    switch (status) {
      case 'APPROVED': return 'bg-success';
      case 'REQUESTED': return 'bg-warning text-dark';
      case 'REJECTED': return 'bg-danger';
      default: return 'bg-secondary';
    }
  }

  // Separate bookings by category
  // Pending: REQUESTED + ride exists + ride is OPEN + ride is in future
  const pendingBookings = $derived(bookings.filter(b => {
    const ride = getRide(b.rideId);
    if (!ride) return false;
    if (b.status !== 'REQUESTED') return false;
    if (ride.status !== 'OPEN') return false;
    return new Date(ride.departureTime) > new Date();
  }));
  
  // Expired/orphaned bookings (requested but ride is gone, full, or in the past)
  const expiredBookings = $derived(bookings.filter(b => {
    if (b.status !== 'REQUESTED') return false;
    const ride = getRide(b.rideId);
    if (!ride) return true;
    if (ride.status !== 'OPEN') return true;
    return new Date(ride.departureTime) <= new Date();
  }));
  
  const upcomingBookings = $derived(bookings.filter(b => {
    const ride = getRide(b.rideId);
    if (!ride) return false;
    return b.status === 'APPROVED' && ride.status !== 'COMPLETED';
  }));
  
  const pastBookings = $derived(bookings.filter(b => {
    const ride = getRide(b.rideId);
    if (!ride) return false;
    return ride.status === 'COMPLETED' && b.status === 'APPROVED';
  }));
  
  const rejectedBookings = $derived(bookings.filter(b => b.status === 'REJECTED'));
</script>

<h1 class="mt-3">My Bookings</h1>

{#if bookings.length === 0}
  <div class="alert alert-info">
    You have no bookings yet. <a href="/rides">Browse available rides</a> to book one.
  </div>
{:else}

  <!-- Pending Bookings (only future rides that are OPEN) -->
  {#if pendingBookings.length > 0}
    <h5 class="mt-4">
      <span class="badge bg-warning text-dark me-2">{pendingBookings.length}</span>
      Pending
    </h5>
    <div class="row">
      {#each pendingBookings as booking}
        {@const ride = getRide(booking.rideId)}
        <div class="col-md-6 col-lg-4 mb-3">
          <div class="card h-100" style="cursor: pointer;" onclick={() => window.location.href = `/rides/${booking.rideId}`}>
            <div class="card-body">
              <h6 class="card-title">{ride.startLocation} → {ride.endLocation}</h6>
              <p class="card-text mb-1"><small class="text-muted">{formatDate(ride.departureTime)}</small></p>
              <p class="card-text mb-1"><small>Driver: {getDriverName(ride.driverId)}</small></p>
              <p class="card-text"><small>CHF {ride.pricePerSeat}</small></p>
            </div>
            <div class="card-footer">
              <span class="badge {getStatusClass(booking.status)}">{booking.status}</span>
              <small class="text-muted ms-2">Waiting for driver approval</small>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}

  <!-- Upcoming Bookings -->
  {#if upcomingBookings.length > 0}
    <h5 class="mt-4">
      <span class="badge bg-success me-2">{upcomingBookings.length}</span>
      Upcoming
    </h5>
    <div class="row">
      {#each upcomingBookings as booking}
        {@const ride = getRide(booking.rideId)}
        <div class="col-md-6 col-lg-4 mb-3">
          <div class="card h-100" style="cursor: pointer;" onclick={() => window.location.href = `/rides/${booking.rideId}`}>
            <div class="card-body">
              <h6 class="card-title">{ride.startLocation} → {ride.endLocation}</h6>
              <p class="card-text mb-1"><small class="text-muted">{formatDate(ride.departureTime)}</small></p>
              <p class="card-text mb-1"><small>Driver: {getDriverName(ride.driverId)}</small></p>
              <p class="card-text"><small>CHF {ride.pricePerSeat}</small></p>
            </div>
            <div class="card-footer">
              <span class="badge {getStatusClass(booking.status)}">{booking.status}</span>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}

  <!-- Past Bookings -->
  {#if pastBookings.length > 0}
    <h5 class="mt-4">
      <span class="badge bg-secondary me-2">{pastBookings.length}</span>
      Past Rides
    </h5>
    <div class="row">
      {#each pastBookings as booking}
        {@const ride = getRide(booking.rideId)}
        <div class="col-md-6 col-lg-4 mb-3">
          <div class="card h-100" style="cursor: pointer;" onclick={() => window.location.href = `/rides/${booking.rideId}`}>
            <div class="card-body">
              <h6 class="card-title">{ride.startLocation} → {ride.endLocation}</h6>
              <p class="card-text mb-1"><small class="text-muted">{formatDate(ride.departureTime)}</small></p>
              <p class="card-text mb-1"><small>Driver: {getDriverName(ride.driverId)}</small></p>
              <p class="card-text"><small>CHF {ride.pricePerSeat}</small></p>
            </div>
            <div class="card-footer">
              <span class="badge bg-secondary">COMPLETED</span>
              <small class="text-primary ms-2">Click to leave a review</small>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}

  <!-- Rejected Bookings -->
  {#if rejectedBookings.length > 0}
    <h5 class="mt-4">
      <span class="badge bg-danger me-2">{rejectedBookings.length}</span>
      Rejected
    </h5>
    <div class="row">
      {#each rejectedBookings as booking}
        {@const ride = getRide(booking.rideId)}
        <div class="col-md-6 col-lg-4 mb-3">
          <div class="card h-100">
            <div class="card-body">
              {#if ride}
                <h6 class="card-title">{ride.startLocation} → {ride.endLocation}</h6>
                <p class="card-text mb-1"><small class="text-muted">{formatDate(ride.departureTime)}</small></p>
                <p class="card-text"><small>Driver: {getDriverName(ride.driverId)}</small></p>
              {:else}
                <h6 class="card-title text-muted">Ride no longer available</h6>
              {/if}
            </div>
            <div class="card-footer">
              <span class="badge {getStatusClass(booking.status)}">{booking.status}</span>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}

  <!-- Expired Bookings (ride gone, full, or in the past) -->
  {#if expiredBookings.length > 0}
    <h5 class="mt-4">
      <span class="badge bg-dark me-2">{expiredBookings.length}</span>
      Expired
    </h5>
    <p class="text-muted small">These bookings are for rides that are full, have departed, or no longer exist.</p>
    <div class="row">
      {#each expiredBookings as booking}
        {@const ride = getRide(booking.rideId)}
        <div class="col-md-6 col-lg-4 mb-3">
          <div class="card h-100 bg-light">
            <div class="card-body">
              {#if ride}
                <h6 class="card-title text-muted">{ride.startLocation} → {ride.endLocation}</h6>
                <p class="card-text mb-1"><small class="text-muted">{formatDate(ride.departureTime)}</small></p>
                <p class="card-text"><small class="text-muted">Driver: {getDriverName(ride.driverId)}</small></p>
              {:else}
                <h6 class="card-title text-muted">Ride no longer available</h6>
                <p class="card-text"><small class="text-muted">This ride has been removed</small></p>
              {/if}
            </div>
            <div class="card-footer">
              <span class="badge bg-dark">EXPIRED</span>
            </div>
          </div>
        </div>
      {/each}
    </div>
  {/if}

{/if}