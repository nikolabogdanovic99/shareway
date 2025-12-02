<script>
  let { data } = $props();

  let rides = $state(data.rides);
  let users = $state(data.users);

  // Helper function to get driver name
  function getDriverName(driverId) {
    const driver = users.find(u => u.id === driverId);
    return driver ? driver.name : 'Unknown';
  }

  // Format date
  function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('de-CH') + ' ' + date.toLocaleTimeString('de-CH', { hour: '2-digit', minute: '2-digit' });
  }
</script>

<h1 class="mt-3">Available Rides</h1>

{#if rides.length === 0}
  <div class="alert alert-info">
    No rides available at the moment.
  </div>
{:else}
  <table class="table table-striped">
    <thead>
      <tr>
        <th>From</th>
        <th>To</th>
        <th>Departure</th>
        <th>Driver</th>
        <th>Price/Seat</th>
        <th>Seats Free</th>
        <th>Status</th>
      </tr>
    </thead>
    <tbody>
      {#each rides as ride}
        <tr>
          <td>{ride.startLocation}</td>
          <td>{ride.endLocation}</td>
          <td>{formatDate(ride.departureTime)}</td>
          <td>{getDriverName(ride.driverId)}</td>
          <td>CHF {ride.pricePerSeat}</td>
          <td>{ride.seatsFree} / {ride.seatsTotal}</td>
          <td>
            <span class="badge" class:bg-success={ride.status === 'OPEN'} 
                                class:bg-warning={ride.status === 'FULL'}
                                class:bg-primary={ride.status === 'IN_PROGRESS'}
                                class:bg-secondary={ride.status === 'COMPLETED'}>
              {ride.status}
            </span>
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
{/if}