<script>
  import { enhance } from "$app/forms";
  import { invalidateAll } from "$app/navigation";
  import LocationAutocomplete from "$lib/components/LocationAutocomplete.svelte";

  let { data, form } = $props();

  let vehicles = $state(data.vehicles || []);
  let rides = $state(data.rides || []);
  let bookings = $state(data.bookings || []);
  let users = $state(data.users || []);
  let dbUser = $state(data.dbUser);
  let user = $state(data.user);
  let isAuthenticated = $state(data.isAuthenticated);

  let activeTab = $state("rides");

  // Collapsible states for booking sections
  let showCanceled = $state(false);
  let showApproved = $state(false);
  let showRejected = $state(false);
  
  // Ride form states
  let startLocation = $state("");
  let endLocation = $state("");
  let routeRadiusKm = $state(5);

  // Vehicle form states
  let selectedMake = $state("");
  let selectedModel = $state("");
  let selectedCanton = $state("");
  let plateNumber = $state("");

  // Car makes and models
  const makes = [
    "Audi",
    "BMW",
    "Ford",
    "Mercedes",
    "Opel",
    "Renault",
    "Skoda",
    "Tesla",
    "Toyota",
    "VW",
  ];

  const models = {
    Audi: ["A1", "A3", "A4", "A6", "Q3", "Q5", "Q7", "e-tron"],
    BMW: ["1er", "2er", "3er", "5er", "X1", "X3", "X5", "i3", "i4"],
    Ford: ["Fiesta", "Focus", "Kuga", "Mustang", "Puma", "Transit"],
    Mercedes: [
      "A-Klasse",
      "B-Klasse",
      "C-Klasse",
      "E-Klasse",
      "GLA",
      "GLC",
      "EQA",
      "EQC",
    ],
    Opel: ["Astra", "Corsa", "Crossland", "Grandland", "Mokka", "Zafira"],
    Renault: ["Captur", "Clio", "Kadjar", "Megane", "Scenic", "Twingo", "Zoe"],
    Skoda: ["Fabia", "Kamiq", "Karoq", "Kodiaq", "Octavia", "Superb", "Enyaq"],
    Tesla: ["Model 3", "Model S", "Model X", "Model Y"],
    Toyota: ["Auris", "Aygo", "C-HR", "Corolla", "RAV4", "Yaris", "Prius"],
    VW: [
      "Golf",
      "ID.3",
      "ID.4",
      "Passat",
      "Polo",
      "T-Cross",
      "T-Roc",
      "Tiguan",
    ],
  };

  // Colors
  const colors = [
    "Schwarz",
    "Weiss",
    "Silber",
    "Grau",
    "Rot",
    "Blau",
    "Grün",
    "Braun",
    "Beige",
    "Orange",
    "Gelb",
  ];

  // Years (2010 - current year)
  const currentYear = new Date().getFullYear();
  const years = Array.from(
    { length: currentYear - 2009 },
    (_, i) => currentYear - i,
  );

  // Swiss cantons
  const cantons = [
    "AG",
    "AI",
    "AR",
    "BE",
    "BL",
    "BS",
    "FR",
    "GE",
    "GL",
    "GR",
    "JU",
    "LU",
    "NE",
    "NW",
    "OW",
    "SG",
    "SH",
    "SO",
    "SZ",
    "TG",
    "TI",
    "UR",
    "VD",
    "VS",
    "ZG",
    "ZH",
  ];

  // Get models for selected make
  const availableModels = $derived(
    selectedMake ? models[selectedMake] || [] : [],
  );

  // Combined plate hash
  const plateHash = $derived(
    selectedCanton && plateNumber ? `${selectedCanton} ${plateNumber}` : "",
  );

  // Validate plate number (1-6 digits only)
  function validatePlateNumber(event) {
    let value = event.target.value.replace(/\D/g, "");
    if (value.length > 6) {
      value = value.slice(0, 6);
    }
    plateNumber = value;
  }

  // Reset model when make changes
  $effect(() => {
    if (selectedMake) {
      selectedModel = "";
    }
  });

  // Reset forms after success
  $effect(() => {
    if (form?.success && form?.action === "vehicle") {
      selectedMake = "";
      selectedModel = "";
      selectedCanton = "";
      plateNumber = "";
    }
    if (form?.success && form?.action === "ride") {
      startLocation = "";
      endLocation = "";
      routeRadiusKm = 5;
    }
  });

  $effect(() => {
    vehicles = data.vehicles || [];
    rides = data.rides || [];
    bookings = data.bookings || [];
    users = data.users || [];
    dbUser = data.dbUser;
    user = data.user;
    isAuthenticated = data.isAuthenticated;
  });

  // Handle form submission with data refresh
  function handleSubmit() {
    return async ({ result, update }) => {
      await update();
      if (result.type === "success") {
        await invalidateAll();
      }
    };
  }

  const isDriverOrAdmin = $derived(
    isAuthenticated &&
      user?.user_roles &&
      (user.user_roles.includes("user") || user.user_roles.includes("admin")),
  );

  const isAdmin = $derived(
    isAuthenticated && user?.user_roles?.includes("admin"),
  );
  const isProfileComplete = $derived(dbUser?.firstName && dbUser?.lastName);
  const isVerified = $derived(
    isAdmin || dbUser?.verificationStatus === "VERIFIED",
  );
  const verificationStatus = $derived(
    dbUser?.verificationStatus || "UNVERIFIED",
  );

  // Get default datetime
  function getDefaultDateTime() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(10, 0, 0, 0);
    return tomorrow.toISOString().slice(0, 16);
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

  // Get rider name
  function getRiderName(riderId) {
    if (!users || users.length === 0) return "Unknown";
    const rider = users.find((u) => u.email === riderId);
    if (!rider) return "Unknown";
    if (rider.firstName && rider.lastName) {
      return `${rider.firstName} ${rider.lastName}`;
    }
    return rider.name || "Unknown";
  }

  // Get ride for booking
  function getRide(rideId) {
    return rides.find((r) => r.id === rideId);
  }

  // Format duration
  function formatDuration(minutes) {
    if (!minutes) return "-";
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0 && mins > 0) return `${hours}h ${mins}min`;
    if (hours > 0) return `${hours}h`;
    return `${mins}min`;
  }

  // Booking categories
  const pendingBookings = $derived(
    bookings.filter((b) => b.status === "REQUESTED"),
  );
  const canceledByRider = $derived(
    bookings.filter((b) => b.status === "CANCELED"),
  );
  const approvedBookings = $derived(
    bookings.filter((b) => b.status === "APPROVED"),
  );
  const rejectedBookings = $derived(
    bookings.filter((b) => b.status === "REJECTED"),
  );
  const openRides = $derived(rides.filter((r) => r.status === "OPEN"));
</script>

{#if !isDriverOrAdmin}
  <div class="alert alert-warning mt-3">Only drivers can access this page.</div>
{:else if !isProfileComplete}
  <div class="alert alert-warning mt-3">
    <strong>Profile incomplete!</strong>
    Please <a href="/account">complete your profile</a> first.
  </div>
{:else if !isVerified}
  <!-- Verification Required Screen -->
  <h1 class="mt-3">Driver Dashboard</h1>

  <div class="card mt-4">
    <div class="card-body text-center py-5">
      {#if verificationStatus === "UNVERIFIED"}
        <div class="mb-4">
          <span style="font-size: 4rem;">🪪</span>
        </div>
        <h3>Verification Required</h3>
        <p class="text-muted mb-4">
          To access the Driver Dashboard and create rides, you need to verify
          your identity first.
        </p>
        <a href="/account" class="btn btn-primary btn-lg">
          Start Verification
        </a>
      {:else if verificationStatus === "PENDING"}
        <div class="mb-4">
          <span style="font-size: 4rem;">⏳</span>
        </div>
        <h3>Verification Pending</h3>
        <p class="text-muted mb-4">
          Your verification request is being reviewed. This usually takes 1-2
          business days.<br />
          You'll get full access once an admin approves your documents.
        </p>
        <a href="/account" class="btn btn-outline-secondary">
          View Verification Status
        </a>
      {:else if verificationStatus === "DENIED"}
        <div class="mb-4">
          <span style="font-size: 4rem;">❌</span>
        </div>
        <h3>Verification Denied</h3>
        <p class="text-muted mb-4">
          Unfortunately, your verification was denied.<br />
          Please upload new documents and try again.
        </p>
        <a href="/account" class="btn btn-primary btn-lg">
          Upload New Documents
        </a>
      {/if}
    </div>
  </div>

  <div class="row mt-4">
    <div class="col-md-4 text-center">
      <div class="card h-100">
        <div class="card-body">
          <h5>🚗 Add Vehicles</h5>
          <p class="text-muted small">Register your vehicle to offer rides.</p>
        </div>
      </div>
    </div>
    <div class="col-md-4 text-center">
      <div class="card h-100">
        <div class="card-body">
          <h5>📍 Create Rides</h5>
          <p class="text-muted small">Offer rides and set your price.</p>
        </div>
      </div>
    </div>
    <div class="col-md-4 text-center">
      <div class="card h-100">
        <div class="card-body">
          <h5>✅ Manage Bookings</h5>
          <p class="text-muted small">Approve or reject booking requests.</p>
        </div>
      </div>
    </div>
  </div>
{:else}
  <!-- Full Dashboard Access -->
  <h1 class="mt-3">Driver Dashboard</h1>

  {#if form?.success}
    <div class="alert alert-success">
      {#if form.action === "vehicle"}Vehicle created successfully!
      {:else if form.action === "deleteVehicle"}Vehicle deleted successfully!
      {:else if form.action === "ride"}Ride created successfully!
      {:else if form.action === "approve"}Booking approved!
      {:else if form.action === "reject"}Booking rejected!
      {:else if form.action === "cancelRide"}Ride canceled successfully!
      {/if}
    </div>
  {/if}

  {#if form?.error}
    <div class="alert alert-danger">{form.error}</div>
  {/if}

  <!-- Tabs -->
  <ul class="nav nav-tabs mb-4">
    <li class="nav-item">
      <button
        class="nav-link"
        class:active={activeTab === "rides"}
        onclick={() => (activeTab = "rides")}
      >
        My Rides
        {#if openRides.length > 0}
          <span class="badge bg-success ms-1">{openRides.length}</span>
        {/if}
      </button>
    </li>
    <li class="nav-item">
      <button
        class="nav-link"
        class:active={activeTab === "requests"}
        onclick={() => (activeTab = "requests")}
      >
        Booking Requests
        {#if pendingBookings.length > 0}
          <span class="badge bg-warning text-dark ms-1">{pendingBookings.length}</span>
        {/if}
        {#if canceledByRider.length > 0}
          <span class="badge bg-secondary ms-1">{canceledByRider.length} canceled</span>
        {/if}
      </button>
    </li>
    <li class="nav-item">
      <button
        class="nav-link"
        class:active={activeTab === "vehicles"}
        onclick={() => (activeTab = "vehicles")}
      >
        My Vehicles
        <span class="badge bg-secondary ms-1">{vehicles.length}</span>
      </button>
    </li>
  </ul>

  <!-- My Rides Tab -->
  {#if activeTab === "rides"}
    {#if vehicles.length === 0}
      <div class="alert alert-warning">
        <strong>No vehicles!</strong> Add a vehicle first before creating rides.
        <button
          class="btn btn-sm btn-primary ms-2"
          onclick={() => (activeTab = "vehicles")}>Add Vehicle</button
        >
      </div>
    {:else}
      <!-- Create Ride Form -->
      <div class="card mb-4">
        <div class="card-header">
          <h5 class="mb-0">Create New Ride</h5>
        </div>
        <div class="card-body">
          <form method="POST" action="?/createRide" use:enhance={handleSubmit}>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label" for="vehicleId">Vehicle *</label>
                <select
                  class="form-select"
                  id="vehicleId"
                  name="vehicleId"
                  required
                >
                  <option value="">Select vehicle...</option>
                  {#each vehicles as vehicle}
                    <option value={vehicle.id}
                      >{vehicle.make}
                      {vehicle.model} ({vehicle.seats} seats)</option
                    >
                  {/each}
                </select>
              </div>
              <div class="col-md-3">
                <label class="form-label" for="departureTime">Departure *</label>
                <input
                  class="form-control"
                  id="departureTime"
                  name="departureTime"
                  type="datetime-local"
                  value={getDefaultDateTime()}
                  required
                />
              </div>
              <div class="col-md-3">
                <label class="form-label" for="durationMinutes">Duration (min) *</label>
                <input
                  class="form-control"
                  id="durationMinutes"
                  name="durationMinutes"
                  type="number"
                  min="15"
                  max="720"
                  value="60"
                  required
                />
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-md-4">
                <label class="form-label" for="startLocation">From *</label>
                <LocationAutocomplete
                  id="startLocation"
                  name="startLocation"
                  placeholder="e.g. Zürich HB"
                  bind:value={startLocation}
                  required={true}
                />
              </div>
              <div class="col-md-4">
                <label class="form-label" for="endLocation">To *</label>
                <LocationAutocomplete
                  id="endLocation"
                  name="endLocation"
                  placeholder="e.g. Bern"
                  bind:value={endLocation}
                  required={true}
                />
              </div>
              <div class="col-md-2">
                <label class="form-label" for="pricePerSeat">Price (CHF) *</label>
                <input
                  class="form-control"
                  id="pricePerSeat"
                  name="pricePerSeat"
                  type="number"
                  min="0"
                  step="0.50"
                  value="15"
                  required
                />
              </div>
              <div class="col-md-2">
                <label class="form-label" for="seatsTotal">Seats *</label>
                <input
                  class="form-control"
                  id="seatsTotal"
                  name="seatsTotal"
                  type="number"
                  min="1"
                  max="8"
                  value="3"
                  required
                />
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <label class="form-label" for="description">Description (optional)</label>
                <input
                  class="form-control"
                  id="description"
                  name="description"
                  type="text"
                  placeholder="e.g. No smoking, pets welcome..."
                />
              </div>
              <div class="col-md-6">
                <label class="form-label" for="routeRadiusKm">
                  Max. Pickup Detour: <strong>{routeRadiusKm} km</strong>
                </label>
                <input
                  class="form-range"
                  id="routeRadiusKm"
                  name="routeRadiusKm"
                  type="range"
                  min="1"
                  max="20"
                  bind:value={routeRadiusKm}
                />
                <small class="text-muted">How far are you willing to detour for pickups?</small>
              </div>
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
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {#each rides as ride}
            <tr>
              <td
                style="cursor: pointer;"
                onclick={() => (window.location.href = `/rides/${ride.id}`)}
              >{ride.startLocation}</td>
              <td
                style="cursor: pointer;"
                onclick={() => (window.location.href = `/rides/${ride.id}`)}
              >{ride.endLocation}</td>
              <td>{formatDate(ride.departureTime)}</td>
              <td>{formatDuration(ride.durationMinutes)}</td>
              <td>CHF {ride.pricePerSeat}</td>
              <td>{ride.seatsFree} / {ride.seatsTotal}</td>
              <td>
                <span
                  class="badge"
                  class:bg-success={ride.status === "OPEN"}
                  class:bg-warning={ride.status === "FULL"}
                  class:bg-primary={ride.status === "IN_PROGRESS"}
                  class:bg-secondary={ride.status === "COMPLETED" || ride.status === "CANCELED"}
                >
                  {ride.status}
                </span>
              </td>
              <td>
                {#if ride.status === "OPEN" || ride.status === "FULL"}
                  <form
                    method="POST"
                    action="?/cancelRide"
                    use:enhance={handleSubmit}
                    class="d-inline"
                  >
                    <input type="hidden" name="rideId" value={ride.id} />
                    <button type="submit" class="btn btn-outline-danger btn-sm">Cancel</button>
                  </form>
                {/if}
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    {/if}
  {/if}

  <!-- Booking Requests Tab -->
  {#if activeTab === "requests"}
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
                <h6 class="card-title">
                  {ride?.startLocation} → {ride?.endLocation}
                </h6>
                <p class="card-text mb-1">
                  <small class="text-muted">{formatDate(ride?.departureTime)}</small>
                </p>
                <hr class="my-2" />
                <p class="card-text mb-1">
                  <strong>👤 Rider:</strong>
                  {getRiderName(booking.riderId)}
                </p>
                {#if booking.pickupLocation}
                  <p class="card-text mb-1">
                    <strong>📍 Pickup:</strong>
                    {booking.pickupLocation}
                  </p>
                {/if}
                {#if booking.message}
                  <p class="card-text mb-1"><strong>💬 Message:</strong></p>
                  <p class="card-text text-muted fst-italic">"{booking.message}"</p>
                {/if}
                <p class="card-text">
                  <small>Seats requested: {booking.seatsBooked || booking.seats}</small>
                </p>
              </div>
              <div class="card-footer d-flex gap-2">
                <form
                  method="POST"
                  action="?/approveBooking"
                  use:enhance={handleSubmit}
                  class="d-inline"
                >
                  <input type="hidden" name="bookingId" value={booking.id} />
                  <button type="submit" class="btn btn-success btn-sm">✓ Approve</button>
                </form>
                <form
                  method="POST"
                  action="?/rejectBooking"
                  use:enhance={handleSubmit}
                  class="d-inline"
                >
                  <input type="hidden" name="bookingId" value={booking.id} />
                  <button type="submit" class="btn btn-danger btn-sm">✗ Reject</button>
                </form>
              </div>
            </div>
          </div>
        {/each}
      </div>
    {/if}

    <!-- Canceled by Rider (Collapsible) -->
    {#if canceledByRider.length > 0}
      <div class="mt-4">
        <h5 
          class="d-flex align-items-center" 
          style="cursor: pointer;"
          onclick={() => (showCanceled = !showCanceled)}
        >
          <span class="me-2">{showCanceled ? '▼' : '►'}</span>
          <span class="badge bg-secondary me-2">{canceledByRider.length}</span>
          Canceled by Rider
        </h5>
        {#if showCanceled}
          <div class="alert alert-warning mt-2">
            <small>⚠️ These bookings were canceled by the rider.</small>
          </div>
          <table class="table table-sm">
            <thead>
              <tr>
                <th>Ride</th>
                <th>Rider</th>
                <th>Pickup</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {#each canceledByRider as booking}
                {@const ride = getRide(booking.rideId)}
                <tr>
                  <td>{ride?.startLocation} → {ride?.endLocation}</td>
                  <td>{getRiderName(booking.riderId)}</td>
                  <td>{booking.pickupLocation || "-"}</td>
                  <td><span class="badge bg-secondary">CANCELED</span></td>
                </tr>
              {/each}
            </tbody>
          </table>
        {/if}
      </div>
    {/if}

    <!-- Approved Bookings (Collapsible) -->
    {#if approvedBookings.length > 0}
      <div class="mt-4">
        <h5 
          class="d-flex align-items-center" 
          style="cursor: pointer;"
          onclick={() => (showApproved = !showApproved)}
        >
          <span class="me-2">{showApproved ? '▼' : '►'}</span>
          <span class="badge bg-success me-2">{approvedBookings.length}</span>
          Approved
        </h5>
        {#if showApproved}
          <table class="table table-sm mt-2">
            <thead>
              <tr>
                <th>Ride</th>
                <th>Rider</th>
                <th>Pickup</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {#each approvedBookings as booking}
                {@const ride = getRide(booking.rideId)}
                <tr>
                  <td>{ride?.startLocation} → {ride?.endLocation}</td>
                  <td>{getRiderName(booking.riderId)}</td>
                  <td>{booking.pickupLocation || "-"}</td>
                  <td><span class="badge bg-success">APPROVED</span></td>
                </tr>
              {/each}
            </tbody>
          </table>
        {/if}
      </div>
    {/if}

    <!-- Rejected Bookings (Collapsible) -->
    {#if rejectedBookings.length > 0}
      <div class="mt-4">
        <h5 
          class="d-flex align-items-center" 
          style="cursor: pointer;"
          onclick={() => (showRejected = !showRejected)}
        >
          <span class="me-2">{showRejected ? '▼' : '►'}</span>
          <span class="badge bg-danger me-2">{rejectedBookings.length}</span>
          Rejected
        </h5>
        {#if showRejected}
          <table class="table table-sm mt-2">
            <thead>
              <tr>
                <th>Ride</th>
                <th>Rider</th>
                <th>Pickup</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {#each rejectedBookings as booking}
                {@const ride = getRide(booking.rideId)}
                <tr>
                  <td>{ride?.startLocation} → {ride?.endLocation}</td>
                  <td>{getRiderName(booking.riderId)}</td>
                  <td>{booking.pickupLocation || "-"}</td>
                  <td><span class="badge bg-danger">REJECTED</span></td>
                </tr>
              {/each}
            </tbody>
          </table>
        {/if}
      </div>
    {/if}
  {/if}

  <!-- Vehicles Tab -->
  {#if activeTab === "vehicles"}
    <!-- Add Vehicle Form -->
    <div class="card mb-4">
      <div class="card-header">
        <h5 class="mb-0">Add New Vehicle</h5>
      </div>
      <div class="card-body">
        <form method="POST" action="?/createVehicle" use:enhance={handleSubmit}>
          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label" for="make">Make (Brand) *</label>
              <select
                class="form-select"
                id="make"
                name="make"
                bind:value={selectedMake}
                required
              >
                <option value="">Select make...</option>
                {#each makes as make}
                  <option value={make}>{make}</option>
                {/each}
              </select>
            </div>
            <div class="col-md-6">
              <label class="form-label" for="model">Model *</label>
              <select
                class="form-select"
                id="model"
                name="model"
                bind:value={selectedModel}
                required
                disabled={!selectedMake}
              >
                <option value="">Select model...</option>
                {#each availableModels as model}
                  <option value={model}>{model}</option>
                {/each}
              </select>
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-md-3">
              <label class="form-label" for="year">Year *</label>
              <select class="form-select" id="year" name="year" required>
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
            <div class="col-md-2">
              <label class="form-label" for="seats">Seats *</label>
              <input
                class="form-control"
                id="seats"
                name="seats"
                type="number"
                min="2"
                max="9"
                value="5"
                required
              />
            </div>
            <div class="col-md-4">
              <label class="form-label">License Plate *</label>
              <div class="input-group">
                <select
                  class="form-select"
                  style="max-width: 80px;"
                  bind:value={selectedCanton}
                  required
                >
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
                <p class="card-text">
                  <small class="text-muted">{vehicle.plateHash}</small>
                </p>
              </div>
              <div class="card-footer">
                <form
                  method="POST"
                  action="?/deleteVehicle"
                  use:enhance={handleSubmit}
                >
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