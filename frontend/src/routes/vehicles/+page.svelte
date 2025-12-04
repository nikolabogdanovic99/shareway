<script>
  import { enhance } from "$app/forms";
  
  let { data, form } = $props();
  let { user, isAuthenticated } = data;

  let myVehicles = $state(data.myVehicles);
  let selectedMake = $state('');
  let selectedModel = $state('');
  let selectedCanton = $state('');
  let plateNumber = $state('');

  // Car makes and models
  const makes = ['Audi', 'BMW', 'Ford', 'Mercedes', 'Opel', 'Renault', 'Skoda', 'Tesla', 'Toyota', 'VW'];
  
  const models = {
    'Audi': ['A1', 'A3', 'A4', 'A6', 'Q3', 'Q5', 'Q7', 'e-tron'],
    'BMW': ['1er', '2er', '3er', '5er', 'X1', 'X3', 'X5', 'i3', 'i4'],
    'Ford': ['Fiesta', 'Focus', 'Kuga', 'Mustang', 'Puma', 'Transit'],
    'Mercedes': ['A-Klasse', 'B-Klasse', 'C-Klasse', 'E-Klasse', 'GLA', 'GLC', 'EQA', 'EQC'],
    'Opel': ['Astra', 'Corsa', 'Crossland', 'Grandland', 'Mokka', 'Zafira'],
    'Renault': ['Captur', 'Clio', 'Kadjar', 'Megane', 'Scenic', 'Twingo', 'Zoe'],
    'Skoda': ['Fabia', 'Kamiq', 'Karoq', 'Kodiaq', 'Octavia', 'Superb', 'Enyaq'],
    'Tesla': ['Model 3', 'Model S', 'Model X', 'Model Y'],
    'Toyota': ['Auris', 'Aygo', 'C-HR', 'Corolla', 'RAV4', 'Yaris', 'Prius'],
    'VW': ['Golf', 'ID.3', 'ID.4', 'Passat', 'Polo', 'T-Cross', 'T-Roc', 'Tiguan']
  };

  // Colors
  const colors = ['Schwarz', 'Weiss', 'Silber', 'Grau', 'Rot', 'Blau', 'Grün', 'Braun', 'Beige', 'Orange', 'Gelb'];

  // Years (2010 - current year)
  const currentYear = new Date().getFullYear();
  const years = Array.from({ length: currentYear - 2009 }, (_, i) => currentYear - i);

  // Swiss cantons
  const cantons = ['AG', 'AI', 'AR', 'BE', 'BL', 'BS', 'FR', 'GE', 'GL', 'GR', 'JU', 'LU', 'NE', 'NW', 'OW', 'SG', 'SH', 'SO', 'SZ', 'TG', 'TI', 'UR', 'VD', 'VS', 'ZG', 'ZH'];

  // Get models for selected make
  const availableModels = $derived(selectedMake ? models[selectedMake] || [] : []);

  // Combined plate hash
  const plateHash = $derived(selectedCanton && plateNumber ? `${selectedCanton} ${plateNumber}` : '');

  // Validate plate number (1-6 digits only)
  function validatePlateNumber(event) {
    let value = event.target.value.replace(/\D/g, ''); // Remove non-digits
    if (value.length > 6) {
      value = value.slice(0, 6);
    }
    plateNumber = value;
  }

  // Reset model when make changes
  $effect(() => {
    if (selectedMake) {
      selectedModel = '';
    }
  });

  // Update when data changes
  $effect(() => {
    myVehicles = data.myVehicles;
  });

  // Check if user is driver or admin
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
        <select class="form-select" id="make" name="make" bind:value={selectedMake} required>
          <option value="">Select make...</option>
          {#each makes as make}
            <option value={make}>{make}</option>
          {/each}
        </select>
      </div>
      <div class="col-md-6">
        <label class="form-label" for="model">Model *</label>
        <select class="form-select" id="model" name="model" bind:value={selectedModel} required disabled={!selectedMake}>
          <option value="">Select model...</option>
          {#each availableModels as model}
            <option value={model}>{model}</option>
          {/each}
        </select>
      </div>
    </div>
    <div class="row mb-3">
      <div class="col-md-3">
        <label class="form-label" for="seats">Seats *</label>
        <input class="form-control" id="seats" name="seats" type="number" min="1" max="9" value="4" required />
      </div>
      <div class="col-md-3">
        <label class="form-label" for="year">Year</label>
        <select class="form-select" id="year" name="year">
          <option value="">Select year...</option>
          {#each years as year}
            <option value={year}>{year}</option>
          {/each}
        </select>
      </div>
      <div class="col-md-3">
        <label class="form-label" for="color">Color *</label>
        <select class="form-select" id="color" name="color" required>
          <option value="">Select color...</option>
          {#each colors as color}
            <option value={color}>{color}</option>
          {/each}
        </select>
      </div>
      <div class="col-md-3">
        <label class="form-label">License Plate *</label>
        <div class="input-group">
          <select class="form-select" style="max-width: 80px;" bind:value={selectedCanton} required>
            <option value="">--</option>
            {#each cantons as canton}
              <option value={canton}>{canton}</option>
            {/each}
          </select>
          <input 
            class="form-control" 
            type="text" 
            placeholder="123456" 
            value={plateNumber}
            oninput={validatePlateNumber}
            required 
            minlength="1"
            maxlength="6"
          />
        </div>
        <input type="hidden" name="plateHash" value={plateHash} />
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