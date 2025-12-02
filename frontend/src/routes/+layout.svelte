<script>
  import favicon from "$lib/assets/favicon.svg";
  import "./styles.css";
  let { children } = $props();
</script>

<svelte:head>
  <link rel="icon" href={favicon} />
</svelte:head>

<nav class="navbar navbar-expand-lg bg-light">
  <div class="container-fluid">
    <a class="navbar-brand" href="/">Freelancer4U</a>
    <button
      class="navbar-toggler"
      type="button"
      data-bs-toggle="collapse"
      data-bs-target="#navbarNav"
      aria-controls="navbarNav"
      aria-expanded="false"
      aria-label="Toggle navigation"
    >
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNav">
      <ul class="navbar-nav">
        <li class="nav-item">
          <a class="nav-link" aria-current="page" href="/companies">Companies</a
          >
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/jobs">Jobs</a>
        </li>
      </ul>
    </div>
  </div>
</nav>

<div class="container mt-3">
  {@render children()}
</div>
