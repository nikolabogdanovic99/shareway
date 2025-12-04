<script>
  let { data } = $props();

  let rides = $state(data.rides || []);
  let users = $state(data.users || []);
  let myBookings = $state(data.myBookings || []);
  let currentPage = $state(data.currentPage || 1);
  let nrOfPages = $state(data.nrOfPages || 0);
  let currentUserEmail = $state(data.currentUserEmail || '');
  let dbUser = $state(data.dbUser);
  const pageSize = 10;

  $effect(() => {
    rides = data.rides || [];
    users = data.users || [];
    myBookings = data.myBookings || [];
    currentPage = data.currentPage || 1;
    nrOfPages = data.nrOfPages || 0;
    currentUserEmail = data.currentUserEmail || '';
    dbUser = data.dbUser;
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

  // Get status badge class - FIXED: REQUESTED instead of PENDING
  function getBookingStatusClass(status) {
    switch (status) {
      case 'APPROVED': return 'bg-success';
      case 'REQUESTED': return 'bg-warning text-dark';
      case 'REJECTED': return 'bg-danger';
      default: return 'bg-info';
    }
  }
</script>

<h1 class="mt-3">Available Rides</h1>

{#if !isProfileComplete}
  <div class="alert alert-warning">
    <strong>Profile incomplete!</strong>
    <a href="/account">Complete your profile</a> (name and surname) to book rides.
  </div>
{/if}

{#if !rides || rides.length === 0}
  <div class="alert alert-info">No rides available at the moment.</div>
{:else}
  <p class="text-muted">Click on a ride to view details and book.</p>
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
          <li class="page-item">
            <a class="page-link" class:active={currentPage == i + 1} href="/rides?pageNumber={i + 1}&pageSize={pageSize}">
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