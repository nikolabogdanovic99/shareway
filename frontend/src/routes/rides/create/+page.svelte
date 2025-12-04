<script>
  import { enhance } from "$app/forms";
  
  let { data, form } = $props();
  let { user, isAuthenticated, dbUser } = data;

  let myVehicles = $state(data.myVehicles);
  let myRides = $state(data.myRides);

  $effect(() => {
    myVehicles = data.myVehicles;
    myRides = data.myRides;
  });

  const isDriverOrAdmin = isAuthenticated && user.user_roles && 
    (user.user_roles.includes("driver") || user.user_roles.includes("admin"));

  const isAdmin = isAuthenticated && user.user_roles && user.user_roles.includes("admin");

  // Admin kann immer, Driver nur wenn verifiziert
  const canCreateRides = isAdmin || (isDriverOrAdmin && dbUser?.verificationStatus === 'VERIFIED');

  // Get default datetime (tomorrow at 10:00)
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
    const vehicle = myVehicles.find(v => v.id === vehicleId);
    return vehicle ? `${vehicle.make} ${vehicle.model}` : 'Unknown';
  }
</script>

{#if !isDriverOrAdmin}
  <div class="alert alert-warning">
    Only drivers can create rides.
  </div>
{:else if !canCreateRides}
  <div class="alert alert-warning">
    <strong>Verification required!</strong>
    {#if dbUser?.verificationStatus === 'PENDING'}
      Your verification is pending. Please wait for admin approval.
    {:else}
      Please <a href="/account">complete your profile</a> and get verified before creating rides.
    {/if}
  </div>
{:else if myVehicles.length === 0}
  <div class="alert alert-warning">
    <strong>No vehicles found!</strong> 
    <a href="/vehicles">Create a vehicle first</a> before you can offer rides.
  </div>
{:else}
  <h1 class="mt-3">Create Ride</h1>

  {#if form?.success}
    <div class="alert alert-success alert-dismissible fade show" role="alert">
      Ride created successfully!
    </div>
  {/if}

  {#if form?.error}
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
      {form.error}
    </div>
  {/if}

  <form class="mb-5" method="POST" action="?/createRide" use:enhance>
    <div class="row mb-3">
      <div class="col">
        <label class="form-label" for="vehicleId">Vehicle *</label>
        <select class="form-select" id="vehicleId" name="vehicleId" required>
          <option value="">Select your vehicle...</option>
          {#each myVehicles as vehicle}
            <option value={vehicle.id}>{vehicle.make} {vehicle.model} ({vehicle.seats} seats)</option>
          {/each}
        </select>
      </div>
    </div>

    <div class="row mb-3">
      <div class="col-md-6">
        <label class="form-label" for="startLocation">Start Location *</label>
        <input class="form-control" id="startLocation" name="startLocation" type="text" placeholder="e.g. Zürich HB" required />
      </div>
      <div class="col-md-6">
        <label class="form-label" for="endLocation">End Location *</label>
        <input class="form-control" id="endLocation" name="endLocation" type="text" placeholder="e.g. Bern Bahnhof" required />
      </div>
    </div>

    <div class="row mb-3">
      <div class="col-md-4">
        <label class="form-label" for="departureTime">Departure *</label>
        <input class="form-control" id="departureTime" name="departureTime" type="datetime-local" value={getDefaultDateTime()} required />
      </div>
      <div class="col-md-4">
        <label class="form-label" for="pricePerSeat">Price per Seat (CHF) *</label>
        <input class="form-control" id="pricePerSeat" name="pricePerSeat" type="number" min="0" step="0.50" value="15" required />
      </div>
      <div class="col-md-4">
        <label class="form-label" for="seatsTotal">Available Seats *</label>
        <input class="form-control" id="seatsTotal" name="seatsTotal" type="number" min="1" max="8" value="3" required />
      </div>
    </div>

    <div class="row mb-3">
      <div class="col">
        <label class="form-label" for="description">Description (optional)</label>
        <textarea class="form-control" id="description" name="description" rows="2" placeholder="e.g. No smoking, pets welcome..."></textarea>
      </div>
    </div>

    <button type="submit" class="btn btn-primary">Create Ride</button>
    <a href="/rides" class="btn btn-secondary ms-2">Cancel</a>
  </form>

  <hr />

  <h2>My Rides</h2>
  {#if myRides.length === 0}
    <div class="alert alert-info">You have no rides yet.</div>
  {:else}
    <table class="table table-striped">
      <thead>
        <tr>
          <th>From</th>
          <th>To</th>
          <th>Departure</th>
          <th>Vehicle</th>
          <th>Price</th>
          <th>Seats</th>
          <th>Status</th>
        </tr>
      </thead>
      <tbody>
        {#each myRides as ride}
          <tr>
            <td>{ride.startLocation}</td>
            <td>{ride.endLocation}</td>
            <td>{formatDate(ride.departureTime)}</td>
            <td>{getVehicleName(ride.vehicleId)}</td>
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