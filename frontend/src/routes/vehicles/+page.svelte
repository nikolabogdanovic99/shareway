<script>
  import { enhance } from "$app/forms";
  
  let { data, form } = $props();
  let { user, isAuthenticated } = data;

  let myVehicles = $state(data.myVehicles);

  // Update when data changes
  $effect(() => {
    myVehicles = data.myVehicles;
  });

  // Check if user is driver or admin
  $effect(() => {
    if (!isAuthenticated || !user.user_roles || 
        (!user.user_roles.includes("driver") && !user.user_roles.includes("admin"))) {
      // Could redirect, but for now just show message
    }
  });

  const isDriverOrAdmin = isAuthenticated && user.user_roles && 
    (user.user_roles.includes("driver") || user.user_roles.includes("admin"));
</script>

{#if !isDriverOrAdmin}
  <div class="alert alert-warning">
    Only drivers can manage vehicles.
  </div>
{:else}
  <h1 class="mt-3">Create Vehicle</h1>

  {#if form?.success}
    <div class="alert alert-success alert-dismissible fade show" role="alert">
      Vehicle created successfully!
    </div>
  {/if}

  {#if form?.error}
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
      {form.error}
    </div>
  {/if}

  <form class="mb-5" method="POST" action="?/createVehicle" use:enhance>
    <div class="row mb-3">
      <div class="col-md-6">
        <label class="form-label" for="make">Make (Brand) *</label>
        <input class="form-control" id="make" name="make" type="text" placeholder="e.g. Tesla" required />
      </div>
      <div class="col-md-6">
        <label class="form-label" for="model">Model *</label>
        <input class="form-control" id="model" name="model" type="text" placeholder="e.g. Model 3" required />
      </div>
    </div>
    <div class="row mb-3">
      <div class="col-md-4">
        <label class="form-label" for="seats">Seats *</label>
        <input class="form-control" id="seats" name="seats" type="number" min="1" max="9" value="4" required />
      </div>
      <div class="col-md-4">
        <label class="form-label" for="year">Year</label>
        <input class="form-control" id="year" name="year" type="number" min="1990" max="2030" placeholder="e.g. 2022" />
      </div>
      <div class="col-md-4">
        <label class="form-label" for="color">Color</label>
        <input class="form-control" id="color" name="color" type="text" placeholder="e.g. Red" />
      </div>
    </div>
    <div class="row mb-3">
      <div class="col">
        <label class="form-label" for="plateHash">License Plate *</label>
        <input class="form-control" id="plateHash" name="plateHash" type="text" placeholder="e.g. ZH 123456" required />
      </div>
    </div>
    <button type="submit" class="btn btn-primary">Create Vehicle</button>
  </form>

  <hr />

  <h2>My Vehicles</h2>
  {#if myVehicles.length === 0}
    <div class="alert alert-info">You have no vehicles yet.</div>
  {:else}
    <table class="table table-striped">
      <thead>
        <tr>
          <th>Make</th>
          <th>Model</th>
          <th>Year</th>
          <th>Color</th>
          <th>Seats</th>
          <th>License Plate</th>
        </tr>
      </thead>
      <tbody>
        {#each myVehicles as vehicle}
          <tr>
            <td>{vehicle.make}</td>
            <td>{vehicle.model}</td>
            <td>{vehicle.year || '-'}</td>
            <td>{vehicle.color || '-'}</td>
            <td>{vehicle.seats}</td>
            <td>{vehicle.plateHash}</td>
          </tr>
        {/each}
      </tbody>
    </table>
  {/if}
{/if}