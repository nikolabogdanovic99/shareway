<script>
  import { enhance } from "$app/forms";
  
  let { data, form } = $props();

  let vehicles = $state(data.vehicles);
  let rides = $state(data.rides);
  let bookings = $state(data.bookings);
  let users = $state(data.users);
  let dbUser = $state(data.dbUser);
  let user = $state(data.user);
  let isAuthenticated = $state(data.isAuthenticated);

  let activeTab = $state('rides');

  $effect(() => {
    vehicles = data.vehicles;
    rides = data.rides;
    bookings = data.bookings;
    users = data.users;
    dbUser = data.dbUser;
    user = data.user;
    isAuthenticated = data.isAuthenticated;
  });

  const isDriverOrAdmin = $derived(isAuthenticated && user?.user_roles && 
    (user.user_roles.includes("driver") || user.user_roles.includes("admin")));

  const isAdmin = $derived(isAuthenticated && user?.user_roles?.includes("admin"));
  const isProfileComplete = $derived(dbUser?.firstName && dbUser?.lastName);
  const canCreateRides = $derived(isAdmin || (isDriverOrAdmin && dbUser?.verificationStatus === 'VERIFIED'));

  // Get default datetime
  function getDefaultDateTime() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(10, 0, 0, 0);
    return tomorrow.toISOString().slice(0, 16);
  }

  // Format date
  function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('de-CH') + ' ' + date.toLocaleTimeString('de-CH', { hour: '2-digit', minute: '2-digit' });
  }

  // Get vehicle name
  function getVehicleName(vehicleId) {
    const vehicle = vehicles.find(v => v.id === vehicleId);
    return vehicle ? `${vehicle.make} ${vehicle.model}` : 'Unknown';
  }

  // Get rider name
  function getRiderName(riderId) {
    const rider = users.find(u => u.email === riderId);
    if (!rider) return "Unknown";
    if (rider.firstName && rider.lastName) {
      return `${rider.firstName} ${rider.lastName}`;
    }
    return rider.name || "Unknown";
  }

  // Get ride for booking
  function getRide(rideId) {
    return rides.find(r => r.id === rideId);
  }

  // Format duration
  function formatDuration(minutes) {
    if (!minutes) return '-';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0 && mins > 0) return `${hours}h ${mins}min`;
    if (hours > 0) return `${hours}h`;
    return `${mins}min`;
  }

  // Pending bookings count
  const pendingBookings = $derived(bookings.filter(b => b.status === 'PENDING'));
  const openRides = $derived(rides.filter(r => r.status === 'OPEN'));
</script>

{#if !isDriverOrAdmin}
  <div class="alert alert-warning mt-3">
    Only drivers can access this page.
  </div>
{:else if !isProfileComplete}
  <div class="alert alert-warning mt-3">
    <strong>Profile incomplete!</strong>
    Please <a href="/account">complete your profile</a> first.
  </div>
{:else}
  <h1 class="mt-3">Driver Dashboard</h1>

  {#if form?.success}
    <div class="alert alert-success">
      {#if form.action === 'vehicle'}Vehicle created successfully!
      {:else if form.action === 'deleteVehicle'}Vehicle deleted successfully!
      {:else if form.action === 'ride'}Ride created successfully!
      {:else if form.action === 'approve'}Booking approved!
      {:else if form.action === 'reject'}Booking rejected!
      {/if}
    </div>
  {/if}

  {#if form?.error}
    <div class="alert alert-danger">{form.error}</div>
  {/if}

  <!-- Tabs -->
  <ul class="nav nav-tabs mb-4">
    <li class="nav-item">
      <button class="nav-link" class:active={activeTab === 'rides'} onclick={() => activeTab = 'rides'}>
        My Rides
        {#if openRides.length > 0}
          <span class="badge bg-success ms-1">{openRides.length}</span>
        {/if}
      </button>
    </li>
    <li class="nav-item">
      <button class="nav-link" class:active={activeTab === 'requests'} onclick={() => activeTab = 'requests'}>
        Booking Requests
        {#if pendingBookings.length > 0}
          <span class="badge bg-warning text-dark ms-1">{pendingBookings.length}</span>
        {/if}
      </button>
    </li>
    <li class="nav-item">
      <button class="nav-link" class:active={activeTab === 'vehicles'} onclick={() => activeTab = 'vehicles'}>
        My Vehicles
        <span class="badge bg-secondary ms-1">{vehicles.length}</span>
      </button>
    </li>
  </ul>

  <!-- My Rides Tab -->
  {#if activeTab === 'rides'}
    {#if !canCreateRides}
      <div class="alert alert-warning">
        <strong>Verification required!</strong>
        {#if dbUser?.verificationStatus === 'PENDING'}
          Your verification is pending. Please wait for admin approval.
        {:else}
          Please <a href="/account">get verified</a> before creating rides.
        {/if}
      </div>
    {:else if vehicles.length === 0}
      <div class="alert alert-warning">
        <strong>No vehicles!</strong> Add a vehicle first before creating rides.
        <button class="btn btn-sm btn-primary ms-2" onclick={() => activeTab = 'vehicles'}>Add Vehicle</button>
      </div>
    {:else}
      <!-- Create Ride Form -->
      <div class="card mb-4">
        <div class="card-header">
          <h5 class="mb-0">Create New Ride</h5>
        </div>
        <div class="card-body">
          <form method="POST" action="?/createRide" use:enhance>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label" for="vehicleId">Vehicle *</label>
                <select class="form-select" id="vehicleId" name="vehicleId" required>
                  <option value="">Select vehicle...</option>
                  {#each vehicles as vehicle}
                    <option value={vehicle.id}>{vehicle.make} {vehicle.model} ({vehicle.seats} seats)</option>
                  {/each}
                </select>
              </div>
              <div class="col-md-3">
                <label class="form-label" for="departureTime">Departure *</label>
                <input class="form-control" id="departureTime" name="departureTime" type="datetime-local" value={getDefaultDateTime()} required />
              </div>
              <div class="col-md-3">
                <label class="form-label" for="durationMinutes">Duration (min) *</label>
                <input class="form-control" id="durationMinutes" name="durationMinutes" type="number" min="15" max="720" value="60" required />
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-md-4">
                <label class="form-label" for="startLocation">From *</label>
                <input class="form-control" id="startLocation" name="startLocation" type="text" placeholder="e.g. Zürich HB" required />
              </div>
              <div class="col-md-4">
                <label class="form-label" for="endLocation">To *</label>
                <input class="form-control" id="endLocation" name="endLocation" type="text" placeholder="e.g. Bern" required />
              </div>
              <div class="col-md-2">
                <label class="form-label" for="pricePerSeat">Price (CHF) *</label>
                <input class="form-control" id="pricePerSeat" name="pricePerSeat" type="number" min="0" step="0.50" value="15" required />
              </div>
              <div class="col-md-2">
                <label class="form-label" for="seatsTotal">Seats *</label>
                <input class="form-control" id="seatsTotal" name="seatsTotal" type="number" min="1" max="8" value="3" required />
              </div>
            </div>
            <div class="mb-3">
              <label class="form-label" for="description">Description (optional)</label>
              <input class="form-control" id="description" name="description" type="text" placeholder="e.g. No smoking, pets welcome..." />
            </div>
            <button type="submit" class="btn btn-primary">Create Ride</button>
          </form>
        </div>
      </div>
    {/if}

    <!-- My Rides List -->
    <h5>My Rides</h5>
    {#if rides.length === 0}
      <div class="alert alert-info">You have no rides yet.</div>
    {:else}
      <table class="table table-striped table-hover">
        <thead>
          <tr>
            <th>From</th>
            <th>To</th>
            <th>Departure</th>
            <th>Duration</th>
            <th>Price</th>
            <th>Seats</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {#each rides as ride}
            <tr style="cursor: pointer;" onclick={() => window.location.href = `/rides/${ride.id}`}>
              <td>{ride.startLocation}</td>
              <td>{ride.endLocation}</td>
              <td>{formatDate(ride.departureTime)}</td>
              <td>{formatDuration(ride.durationMinutes)}</td>
              <td>CHF {ride.pricePerSeat}</td>
              <td>{ride.seatsFree} / {ride.seatsTotal}</td>
              <td>
                <span class="badge"
                  class:bg-success={ride.status === "OPEN"}
                  class:bg-warning={ride.status === "FULL"}
                  class:bg-primary={ride.status === "IN_PROGRESS"}
                  class:bg-secondary={ride.status === "COMPLETED"}>
                  {ride.status}
                </span>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    {/if}
  {/if}

  <!-- Booking Requests Tab -->
  {#if activeTab === 'requests'}
    <h5>Booking Requests</h5>
    {#if pendingBookings.length === 0}
      <div class="alert alert-info">No pending booking requests.</div>
    {:else}
      <div class="row">
        {#each pendingBookings as booking}
          {@const ride = getRide(booking.rideId)}
          <div class="col-md-6 col-lg-4 mb-3">
            <div class="card h-100">
              <div class="card-body">
                <h6 class="card-title">{ride?.startLocation} → {ride?.endLocation}</h6>
                <p class="card-text mb-1"><small class="text-muted">{formatDate(ride?.departureTime)}</small></p>
                <p class="card-text mb-2"><strong>Rider:</strong> {getRiderName(booking.riderId)}</p>
                <p class="card-text"><small>Seats requested: {booking.seatsBooked}</small></p>
              </div>
              <div class="card-footer d-flex gap-2">
                <form method="POST" action="?/approveBooking" use:enhance class="d-inline">
                  <input type="hidden" name="bookingId" value={booking.id} />
                  <button type="submit" class="btn btn-success btn-sm">✓ Approve</button>
                </form>
                <form method="POST" action="?/rejectBooking" use:enhance class="d-inline">
                  <input type="hidden" name="bookingId" value={booking.id} />
                  <button type="submit" class="btn btn-danger btn-sm">✗ Reject</button>
                </form>
              </div>
            </div>
          </div>
        {/each}
      </div>
    {/if}

    <!-- Approved/Rejected Bookings -->
    {@const otherBookings = bookings.filter(b => b.status !== 'PENDING')}
    {#if otherBookings.length > 0}
      <h5 class="mt-4">Past Requests</h5>
      <table class="table table-sm">
        <thead>
          <tr>
            <th>Ride</th>
            <th>Rider</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {#each otherBookings as booking}
            {@const ride = getRide(booking.rideId)}
            <tr>
              <td>{ride?.startLocation} → {ride?.endLocation}</td>
              <td>{getRiderName(booking.riderId)}</td>
              <td>
                <span class="badge" class:bg-success={booking.status === 'APPROVED'} class:bg-danger={booking.status === 'REJECTED'}>
                  {booking.status}
                </span>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    {/if}
  {/if}

  <!-- Vehicles Tab -->
  {#if activeTab === 'vehicles'}
    <!-- Add Vehicle Form -->
    <div class="card mb-4">
      <div class="card-header">
        <h5 class="mb-0">Add New Vehicle</h5>
      </div>
      <div class="card-body">
        <form method="POST" action="?/createVehicle" use:enhance>
          <div class="row mb-3">
            <div class="col-md-3">
              <label class="form-label" for="make">Make *</label>
              <input class="form-control" id="make" name="make" type="text" placeholder="e.g. Toyota" required />
            </div>
            <div class="col-md-3">
              <label class="form-label" for="model">Model *</label>
              <input class="form-control" id="model" name="model" type="text" placeholder="e.g. Corolla" required />
            </div>
            <div class="col-md-2">
              <label class="form-label" for="year">Year *</label>
              <input class="form-control" id="year" name="year" type="number" min="1990" max="2025" value="2020" required />
            </div>
            <div class="col-md-2">
              <label class="form-label" for="color">Color *</label>
              <input class="form-control" id="color" name="color" type="text" placeholder="e.g. Black" required />
            </div>
            <div class="col-md-2">
              <label class="form-label" for="seats">Seats *</label>
              <input class="form-control" id="seats" name="seats" type="number" min="2" max="9" value="5" required />
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-md-4">
              <label class="form-label" for="plateHash">License Plate *</label>
              <input class="form-control" id="plateHash" name="plateHash" type="text" placeholder="e.g. ZH 123456" required />
            </div>
          </div>
          <button type="submit" class="btn btn-primary">Add Vehicle</button>
        </form>
      </div>
    </div>

    <!-- My Vehicles List -->
    <h5>My Vehicles</h5>
    {#if vehicles.length === 0}
      <div class="alert alert-info">You have no vehicles yet.</div>
    {:else}
      <div class="row">
        {#each vehicles as vehicle}
          <div class="col-md-6 col-lg-4 mb-3">
            <div class="card h-100">
              <div class="card-body">
                <h5 class="card-title">{vehicle.make} {vehicle.model}</h5>
                <p class="card-text">
                  <span class="badge bg-light text-dark me-1">{vehicle.year}</span>
                  <span class="badge bg-light text-dark me-1">{vehicle.color}</span>
                  <span class="badge bg-light text-dark">{vehicle.seats} seats</span>
                </p>
                <p class="card-text"><small class="text-muted">{vehicle.plateHash}</small></p>
              </div>
              <div class="card-footer">
                <form method="POST" action="?/deleteVehicle" use:enhance>
                  <input type="hidden" name="vehicleId" value={vehicle.id} />
                  <button type="submit" class="btn btn-outline-danger btn-sm">Delete</button>
                </form>
              </div>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  {/if}
{/if}