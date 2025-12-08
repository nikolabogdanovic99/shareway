<script>
  import { goto } from "$app/navigation";
  
  let { data } = $props();

  let rides = $state(data.rides || []);
  let users = $state(data.users || []);
  let myBookings = $state(data.myBookings || []);
  let currentPage = $state(data.currentPage || 1);
  let nrOfPages = $state(data.nrOfPages || 0);
  let currentUserEmail = $state(data.currentUserEmail || '');
  let dbUser = $state(data.dbUser);
  const pageSize = 10;

  // Filter states
  let filterFrom = $state(data.filterFrom || '');
  let filterTo = $state(data.filterTo || '');
  let filterMaxPrice = $state(data.filterMaxPrice || '');
  let filterDate = $state(data.filterDate || '');

  $effect(() => {
    rides = data.rides || [];
    users = data.users || [];
    myBookings = data.myBookings || [];
    currentPage = data.currentPage || 1;
    nrOfPages = data.nrOfPages || 0;
    currentUserEmail = data.currentUserEmail || '';
    dbUser = data.dbUser;
    filterFrom = data.filterFrom || '';
    filterTo = data.filterTo || '';
    filterMaxPrice = data.filterMaxPrice || '';
    filterDate = data.filterDate || '';
  });

  const isProfileComplete = $derived(dbUser?.firstName && dbUser?.lastName);

  function getDriverName(driverId) {
    if (!users || users.length === 0) return "Unknown";
    const driver = users.find((u) => u.email === driverId);
    if (!driver) return "Unknown";
    if (driver.firstName && driver.lastName) {
      return `${driver.firstName} ${driver.lastName}`;
    }
    return driver.name || "Unknown";
  }

  function formatDate(dateString) {
    if (!dateString) return "-";
    const date = new Date(dateString);
    return date.toLocaleDateString("de-CH") + " " + date.toLocaleTimeString("de-CH", { hour: "2-digit", minute: "2-digit" });
  }

  function getMyBooking(rideId) {
    if (!myBookings || myBookings.length === 0) return null;
    return myBookings.find((b) => b.rideId === rideId);
  }

  function isMyRide(driverId) {
    return currentUserEmail && currentUserEmail === driverId;
  }

  function getBookingStatusClass(status) {
    switch (status) {
      case 'APPROVED': return 'bg-success';
      case 'REQUESTED': return 'bg-warning text-dark';
      case 'REJECTED': return 'bg-danger';
      default: return 'bg-info';
    }
  }

  // Apply filters
  function applyFilters() {
    const params = new URLSearchParams();
    params.set('pageNumber', '1');
    params.set('pageSize', pageSize.toString());
    if (filterFrom) params.set('from', filterFrom);
    if (filterTo) params.set('to', filterTo);
    if (filterMaxPrice) params.set('maxPrice', filterMaxPrice);
    if (filterDate) params.set('date', filterDate);
    goto(`/rides?${params.toString()}`);
  }

  // Clear filters
  function clearFilters() {
    filterFrom = '';
    filterTo = '';
    filterMaxPrice = '';
    filterDate = '';
    goto('/rides');
  }

  // Check if any filter is active
  const hasActiveFilters = $derived(filterFrom || filterTo || filterMaxPrice || filterDate);
</script>

<h1 class="mt-3">Available Rides</h1>

{#if !isProfileComplete}
  <div class="alert alert-warning">
    <strong>Profile incomplete!</strong>
    <a href="/account">Complete your profile</a> (name and surname) to book rides.
  </div>
{/if}

<!-- Filter Section -->
<div class="card mb-4">
  <div class="card-body">
    <h6 class="card-title mb-3">🔍 Filter Rides</h6>
    <div class="row g-3">
      <div class="col-md-3">
        <label class="form-label" for="filterFrom">From</label>
        <input 
          class="form-control" 
          id="filterFrom" 
          type="text" 
          placeholder="e.g. Zürich"
          bind:value={filterFrom}
        />
      </div>
      <div class="col-md-3">
        <label class="form-label" for="filterTo">To</label>
        <input 
          class="form-control" 
          id="filterTo" 
          type="text" 
          placeholder="e.g. Bern"
          bind:value={filterTo}
        />
      </div>
      <div class="col-md-2">
        <label class="form-label" for="filterMaxPrice">Max Price (CHF)</label>
        <input 
          class="form-control" 
          id="filterMaxPrice" 
          type="number" 
          min="0"
          placeholder="e.g. 25"
          bind:value={filterMaxPrice}
        />
      </div>
      <div class="col-md-2">
        <label class="form-label" for="filterDate">Date</label>
        <input 
          class="form-control" 
          id="filterDate" 
          type="date"
          bind:value={filterDate}
        />
      </div>
      <div class="col-md-2 d-flex align-items-end gap-2">
        <button class="btn btn-primary" onclick={applyFilters}>
          Search
        </button>
        {#if hasActiveFilters}
          <button class="btn btn-outline-secondary" onclick={clearFilters}>
            Clear
          </button>
        {/if}
      </div>
    </div>
  </div>
</div>

{#if !rides || rides.length === 0}
  <div class="alert alert-info">
    {#if hasActiveFilters}
      No rides match your filters. <button class="btn btn-link p-0" onclick={clearFilters}>Clear filters</button>
    {:else}
      No rides available at the moment.
    {/if}
  </div>
{:else}
  <p class="text-muted">
    {#if hasActiveFilters}
      Showing filtered results.
    {:else}
      Click on a ride to view details and book.
    {/if}
  </p>
  <table class="table table-striped table-hover">
    <thead>
      <tr>
        <th>From</th>
        <th>To</th>
        <th>Departure</th>
        <th>Driver</th>
        <th>Price/Seat</th>
        <th>Seats Free</th>
      </tr>
    </thead>
    <tbody>
      {#each rides as ride}
        {@const myBooking = getMyBooking(ride.id)}
        <tr style="cursor: pointer;" onclick={() => (window.location.href = `/rides/${ride.id}`)}>
          <td>{ride.startLocation}</td>
          <td>{ride.endLocation}</td>
          <td>{formatDate(ride.departureTime)}</td>
          <td>
            {#if isMyRide(ride.driverId)}
              <span class="badge bg-secondary">Your Ride</span>
            {:else}
              {getDriverName(ride.driverId)}
            {/if}
          </td>
          <td>CHF {ride.pricePerSeat}</td>
          <td>
            {#if myBooking}
              <span class="badge {getBookingStatusClass(myBooking.status)}">{myBooking.status}</span>
            {:else}
              {ride.seatsFree} / {ride.seatsTotal}
            {/if}
          </td>
        </tr>
      {/each}
    </tbody>
  </table>

  {#if nrOfPages > 1}
    <nav>
      <ul class="pagination">
        {#each Array(nrOfPages) as _, i}
          {@const params = new URLSearchParams()}
          {#if filterFrom}{params.set('from', filterFrom)}{/if}
          {#if filterTo}{params.set('to', filterTo)}{/if}
          {#if filterMaxPrice}{params.set('maxPrice', filterMaxPrice)}{/if}
          {#if filterDate}{params.set('date', filterDate)}{/if}
          <li class="page-item">
            <a class="page-link" class:active={currentPage == i + 1} 
               href="/rides?pageNumber={i + 1}&pageSize={pageSize}&{params.toString()}">
              {i + 1}
            </a>
          </li>
        {/each}
      </ul>
    </nav>
  {/if}
{/if}

<style>
  .page-link:focus {
    box-shadow: none;
  }
</style>