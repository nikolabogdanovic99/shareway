<script>
  import { enhance } from "$app/forms";
  import { invalidateAll } from "$app/navigation";
  import LocationAutocomplete from "$lib/components/LocationAutocomplete.svelte";

  let { data, form } = $props();

  let vehicles = $state(data.vehicles || []);
  let rides = $state(data.rides || []);

  let startLocation = $state("");
  let endLocation = $state("");
  let routeRadiusKm = $state(5);

  $effect(() => {
    vehicles = data.vehicles || [];
    rides = data.rides || [];
    
    if (form?.success && form?.action === "ride") {
      startLocation = "";
      endLocation = "";
      routeRadiusKm = 5;
    }
  });

  function handleSubmit() {
    return async ({ result, update }) => {
      await update();
      if (result.type === "success") {
        await invalidateAll();
      }
    };
  }

  function getDefaultDateTime() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(10, 0, 0, 0);
    return tomorrow.toISOString().slice(0, 16);
  }

  function formatDate(dateString) {
    if (!dateString) return "-";
    const date = new Date(dateString);
    return date.toLocaleDateString("de-CH") + " " + date.toLocaleTimeString("de-CH", { hour: "2-digit", minute: "2-digit" });
  }

  function formatDuration(minutes) {
    if (!minutes) return "-";
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0 && mins > 0) return `${hours}h ${mins}min`;
    if (hours > 0) return `${hours}h`;
    return `${mins}min`;
  }
</script>

<div class="mb-4">
  <a href="/driver" class="btn btn-outline-secondary">
    <i class="bi bi-arrow-left me-2"></i>Back to Dashboard
  </a>
</div>

<h2 class="mb-4">
  <i class="bi bi-car-front text-primary me-2"></i>My Rides
</h2>

{#if form?.success}
  <div class="alert alert-success">
    {#if form.action === "ride"}Ride created successfully!
    {:else if form.action === "deleteRide"}Ride deleted successfully!
    {/if}
  </div>
{/if}

{#if form?.error}
  <div class="alert alert-danger">{form.error}</div>
{/if}

{#if vehicles.length === 0}
  <div class="alert alert-warning">
    <i class="bi bi-exclamation-triangle me-2"></i>
    <strong>No vehicles!</strong> Add a vehicle first before creating rides.
    <a href="/driver/vehicles" class="btn btn-sm btn-primary ms-2">Add Vehicle</a>
  </div>
{:else}
  <!-- Create Ride Form -->
  <div class="card mb-4">
    <div class="card-header">
      <h5 class="mb-0"><i class="bi bi-plus-circle me-2"></i>Create New Ride</h5>
    </div>
    <div class="card-body">
      <form method="POST" action="?/createRide" use:enhance={handleSubmit}>
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
            <LocationAutocomplete id="startLocation" name="startLocation" placeholder="e.g. Zürich HB" bind:value={startLocation} required={true} />
          </div>
          <div class="col-md-4">
            <label class="form-label" for="endLocation">To *</label>
            <LocationAutocomplete id="endLocation" name="endLocation" placeholder="e.g. Bern" bind:value={endLocation} required={true} />
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
        <div class="row mb-3">
          <div class="col-md-6">
            <label class="form-label" for="description">Description (optional)</label>
            <input class="form-control" id="description" name="description" type="text" placeholder="e.g. No smoking, pets welcome..." />
          </div>
          <div class="col-md-6">
            <label class="form-label" for="routeRadiusKm">Max. Pickup Detour: <strong>{routeRadiusKm} km</strong></label>
            <input class="form-range" id="routeRadiusKm" name="routeRadiusKm" type="range" min="1" max="20" bind:value={routeRadiusKm} />
          </div>
        </div>
        <button type="submit" class="btn btn-primary">
          <i class="bi bi-plus-lg me-2"></i>Create Ride
        </button>
      </form>
    </div>
  </div>
{/if}

<!-- Rides List -->
<div class="card">
  <div class="card-header">
    <h5 class="mb-0">All My Rides ({rides.length})</h5>
  </div>
  <div class="card-body">
    {#if rides.length === 0}
      <p class="text-muted text-center py-4">You have no rides yet.</p>
    {:else}
      <div class="table-responsive">
        <table class="table table-hover align-middle">
          <thead>
            <tr>
              <th>Route</th>
              <th>Departure</th>
              <th>Duration</th>
              <th>Price</th>
              <th>Seats</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {#each rides as ride}
              <tr>
                <td>
                  <a href="/rides/{ride.id}" class="text-decoration-none">
                    <strong>{ride.startLocation}</strong>
                    <i class="bi bi-arrow-right mx-2 text-primary"></i>
                    <strong>{ride.endLocation}</strong>
                  </a>
                </td>
                <td>{formatDate(ride.departureTime)}</td>
                <td>{formatDuration(ride.durationMinutes)}</td>
                <td><strong>CHF {ride.pricePerSeat}</strong></td>
                <td>{ride.seatsFree} / {ride.seatsTotal}</td>
                <td>
                  <span class="badge" class:bg-success={ride.status === "OPEN"} class:bg-warning={ride.status === "FULL"} class:bg-primary={ride.status === "IN_PROGRESS"} class:bg-secondary={ride.status === "COMPLETED"}>
                    {ride.status}
                  </span>
                </td>
                <td>
                  {#if ride.status === "OPEN" || ride.status === "FULL"}
                    <form method="POST" action="?/deleteRide" use:enhance={handleSubmit} class="d-inline">
                      <input type="hidden" name="rideId" value={ride.id} />
                      <button type="submit" class="btn btn-outline-danger btn-sm" onclick={(e) => { if (!confirm('Delete this ride?')) e.preventDefault(); }}>
                        <i class="bi bi-trash"></i>
                      </button>
                    </form>
                  {/if}
                </td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </div>
</div>