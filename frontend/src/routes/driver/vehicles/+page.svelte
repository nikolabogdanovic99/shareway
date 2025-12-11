<script>
  import { enhance } from "$app/forms";
  import { invalidateAll } from "$app/navigation";
  import vehicleData from "$lib/data/vehicles.json";

  let { data, form } = $props();

  let vehicles = $state(data.vehicles || []);

  let selectedMake = $state("");
  let selectedModel = $state("");
  let selectedCanton = $state("");
  let plateNumber = $state("");

  const { makes, models, types, colors, cantons } = vehicleData;
  const currentYear = new Date().getFullYear();
  const years = Array.from({ length: currentYear - 2009 }, (_, i) => currentYear - i);

  const availableModels = $derived(selectedMake ? models[selectedMake] || [] : []);
  const plateHash = $derived(selectedCanton && plateNumber ? `${selectedCanton} ${plateNumber}` : "");

  function getVehicleType(model) {
    if (types.suv.includes(model)) return 'suv';
    if (types.electric.includes(model)) return 'electric';
    if (types.hatchback.includes(model)) return 'hatchback';
    if (types.sedan.includes(model)) return 'sedan';
    return 'sedan';
  }

  function validatePlateNumber(event) {
    let value = event.target.value.replace(/\D/g, "");
    if (value.length > 6) value = value.slice(0, 6);
    plateNumber = value;
  }

  $effect(() => {
    vehicles = data.vehicles || [];
    if (form?.success && form?.action === "vehicle") {
      selectedMake = "";
      selectedModel = "";
      selectedCanton = "";
      plateNumber = "";
    }
  });

  $effect(() => {
    if (selectedMake) selectedModel = "";
  });

  function handleSubmit() {
    return async ({ result, update }) => {
      await update();
      if (result.type === "success") await invalidateAll();
    };
  }
</script>

<div class="mb-4">
  <a href="/driver" class="btn btn-outline-secondary">
    <i class="bi bi-arrow-left me-2"></i>Back to Dashboard
  </a>
</div>

<h2 class="mb-4">
  <i class="bi bi-truck text-success me-2"></i>My Vehicles
</h2>

{#if form?.success}
  <div class="alert alert-success">
    {#if form.action === "vehicle"}Vehicle added!
    {:else if form.action === "deleteVehicle"}Vehicle deleted!
    {/if}
  </div>
{/if}

{#if form?.error}
  <div class="alert alert-danger">{form.error}</div>
{/if}

<!-- Add Vehicle Form -->
<div class="card mb-4">
  <div class="card-header">
    <h5 class="mb-0"><i class="bi bi-plus-circle me-2"></i>Add New Vehicle</h5>
  </div>
  <div class="card-body">
    <form method="POST" action="?/createVehicle" use:enhance={handleSubmit}>
      <div class="row mb-3">
        <div class="col-md-6">
          <label class="form-label" for="make">Make *</label>
          <select class="form-select" id="make" name="make" bind:value={selectedMake} required>
            <option value="">Select make...</option>
            {#each makes as make}<option value={make}>{make}</option>{/each}
          </select>
        </div>
        <div class="col-md-6">
          <label class="form-label" for="model">Model *</label>
          <select class="form-select" id="model" name="model" bind:value={selectedModel} required disabled={!selectedMake}>
            <option value="">Select model...</option>
            {#each availableModels as model}<option value={model}>{model}</option>{/each}
          </select>
        </div>
      </div>
      <div class="row mb-3">
        <div class="col-md-3">
          <label class="form-label" for="year">Year *</label>
          <select class="form-select" id="year" name="year" required>
            <option value="">Year...</option>
            {#each years as year}<option value={year}>{year}</option>{/each}
          </select>
        </div>
        <div class="col-md-3">
          <label class="form-label" for="color">Color *</label>
          <select class="form-select" id="color" name="color" required>
            <option value="">Color...</option>
            {#each colors as color}<option value={color}>{color}</option>{/each}
          </select>
        </div>
        <div class="col-md-2">
          <label class="form-label" for="seats">Seats *</label>
          <input class="form-control" id="seats" name="seats" type="number" min="2" max="9" value="5" required />
        </div>
        <div class="col-md-4">
          <label class="form-label">License Plate *</label>
          <div class="input-group">
            <select class="form-select" style="max-width: 80px;" bind:value={selectedCanton} required>
              <option value="">--</option>
              {#each cantons as canton}<option value={canton}>{canton}</option>{/each}
            </select>
            <input class="form-control" type="text" placeholder="123456" value={plateNumber} oninput={validatePlateNumber} required maxlength="6" />
          </div>
          <input type="hidden" name="plateHash" value={plateHash} />
        </div>
      </div>
      <button type="submit" class="btn btn-primary">
        <i class="bi bi-plus-lg me-2"></i>Add Vehicle
      </button>
    </form>
  </div>
</div>

<!-- Vehicles List -->
<div class="card">
  <div class="card-header">
    <h5 class="mb-0">My Vehicles ({vehicles.length})</h5>
  </div>
  <div class="card-body">
    {#if vehicles.length === 0}
      <p class="text-muted text-center py-4">You have no vehicles yet.</p>
    {:else}
      <div class="row g-4">
        {#each vehicles as vehicle}
          <div class="col-md-6">
            <div class="card h-100">
              <div class="card-body text-center">
<img 
  src="/images/car-{getVehicleType(vehicle.model)}.png" 
  alt={getVehicleType(vehicle.model)} 
  class="img-fluid mb-3"
  style="max-height: 180px; object-fit: contain;"
/>
                <h5 class="card-title mb-1">{vehicle.make} {vehicle.model}</h5>
                <small class="text-muted text-capitalize d-block mb-2">{getVehicleType(vehicle.model)}</small>
                <p class="mb-2">
                  <span class="badge bg-light text-dark me-1">{vehicle.year}</span>
                  <span class="badge bg-light text-dark me-1">{vehicle.color}</span>
                  <span class="badge bg-light text-dark">{vehicle.seats} seats</span>
                </p>
                <p class="text-muted small mb-0"><i class="bi bi-card-text me-1"></i>{vehicle.plateHash}</p>
              </div>
              <div class="card-footer text-center">
                <form method="POST" action="?/deleteVehicle" use:enhance={handleSubmit}>
                  <input type="hidden" name="vehicleId" value={vehicle.id} />
                  <button type="submit" class="btn btn-outline-danger btn-sm" onclick={(e) => { if (!confirm('Delete vehicle?')) e.preventDefault(); }}>
                    <i class="bi bi-trash me-1"></i>Delete
                  </button>
                </form>
              </div>
            </div>
          </div>
        {/each}
      </div>
    {/if}
  </div>
</div>