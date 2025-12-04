<script>
  import { enhance } from "$app/forms";
  
  let { data, form } = $props();
  let { user, isAuthenticated } = data;

  let myRides = $state(data.myRides);
  let bookings = $state(data.bookings);
  let users = $state(data.users);

  $effect(() => {
    myRides = data.myRides;
    bookings = data.bookings;
    users = data.users;
  });

  const isDriverOrAdmin = isAuthenticated && user.user_roles && 
    (user.user_roles.includes("driver") || user.user_roles.includes("admin"));

  function getRiderName(riderId) {
    const rider = users.find(u => u.id === riderId);
    return rider ? rider.name : riderId;
  }

  function getRide(rideId) {
    return myRides.find(r => r.id === rideId);
  }

  function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('de-CH') + ' ' + date.toLocaleTimeString('de-CH', { hour: '2-digit', minute: '2-digit' });
  }

  const pendingBookings = $derived(bookings.filter(b => b.status === 'REQUESTED'));
  const approvedBookings = $derived(bookings.filter(b => b.status === 'APPROVED'));
  const rejectedBookings = $derived(bookings.filter(b => b.status === 'REJECTED'));
</script>

{#if !isDriverOrAdmin}
  <div class="alert alert-warning">Only drivers can view booking requests.</div>
{:else}
  <h1 class="mt-3">Booking Requests</h1>

  {#if form?.success}
    <div class="alert alert-success">Booking {form.action} successfully!</div>
  {/if}

  {#if form?.error}
    <div class="alert alert-danger">{form.error}</div>
  {/if}

  <h2 class="mt-4">
    <span class="badge bg-warning text-dark me-2">{pendingBookings.length}</span>
    Pending Requests
  </h2>
  
  {#if pendingBookings.length === 0}
    <div class="alert alert-info">No pending booking requests.</div>
  {:else}
    <table class="table table-striped">
      <thead>
        <tr>
          <th>Rider</th>
          <th>Ride</th>
          <th>Departure</th>
          <th>Seats</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {#each pendingBookings as booking}
          {@const ride = getRide(booking.rideId)}
          <tr>
            <td>{getRiderName(booking.riderId)}</td>
            <td>{ride ? `${ride.startLocation} → ${ride.endLocation}` : 'Unknown'}</td>
            <td>{ride ? formatDate(ride.departureTime) : '-'}</td>
            <td>{booking.seats}</td>
            <td>
              <form method="POST" action="?/approveBooking" use:enhance style="display: inline;">
                <input type="hidden" name="bookingId" value={booking.id} />
                <button type="submit" class="btn btn-success btn-sm">✓ Approve</button>
              </form>
              <form method="POST" action="?/rejectBooking" use:enhance style="display: inline;">
                <input type="hidden" name="bookingId" value={booking.id} />
                <button type="submit" class="btn btn-danger btn-sm ms-1">✗ Reject</button>
              </form>
            </td>
          </tr>
        {/each}
      </tbody>
    </table>
  {/if}

  <h2 class="mt-4">
    <span class="badge bg-success me-2">{approvedBookings.length}</span>
    Approved Bookings
  </h2>
  
  {#if approvedBookings.length === 0}
    <div class="alert alert-secondary">No approved bookings yet.</div>
  {:else}
    <table class="table table-striped">
      <thead>
        <tr>
          <th>Rider</th>
          <th>Ride</th>
          <th>Departure</th>
          <th>Seats</th>
        </tr>
      </thead>
      <tbody>
        {#each approvedBookings as booking}
          {@const ride = getRide(booking.rideId)}
          <tr>
            <td>{getRiderName(booking.riderId)}</td>
            <td>{ride ? `${ride.startLocation} → ${ride.endLocation}` : 'Unknown'}</td>
            <td>{ride ? formatDate(ride.departureTime) : '-'}</td>
            <td>{booking.seats}</td>
          </tr>
        {/each}
      </tbody>
    </table>
  {/if}

  {#if rejectedBookings.length > 0}
    <h2 class="mt-4">
      <span class="badge bg-danger me-2">{rejectedBookings.length}</span>
      Rejected Bookings
    </h2>
    
    <table class="table table-striped">
      <thead>
        <tr>
          <th>Rider</th>
          <th>Ride</th>
          <th>Seats</th>
        </tr>
      </thead>
      <tbody>
        {#each rejectedBookings as booking}
          {@const ride = getRide(booking.rideId)}
          <tr>
            <td>{getRiderName(booking.riderId)}</td>
            <td>{ride ? `${ride.startLocation} → ${ride.endLocation}` : 'Unknown'}</td>
            <td>{booking.seats}</td>
          </tr>
        {/each}
      </tbody>
    </table>
  {/if}
{/if}