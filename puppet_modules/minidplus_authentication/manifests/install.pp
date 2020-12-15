#install.pp
class minidplus_authentication::install inherits minidplus_authentication {

  user { $minidplus_authentication::service_name:
    ensure => present,
    shell  => '/sbin/nologin',
    home   => '/',
  } ->
  file { "${minidplus_authentication::config_dir}${minidplus_authentication::application}":
    ensure => 'directory',
    mode   => '0755',
    owner  => $minidplus_authentication::service_name,
    group  => $minidplus_authentication::service_name,
  } ->
  file { "${minidplus_authentication::config_dir}${minidplus_authentication::application}/config":
    ensure => 'directory',
    owner  => $minidplus_authentication::service_name,
    group  => $minidplus_authentication::service_name,
    mode   => '0755',
  } ->
  file { "${minidplus_authentication::log_root}${minidplus_authentication::application}":
    ensure => 'directory',
    mode   => '0755',
    owner  =>  $minidplus_authentication::service_name,
    group  =>  $minidplus_authentication::service_name,
  } ->
  file { "${minidplus_authentication::install_dir}${minidplus_authentication::application}":
    ensure => 'directory',
    mode   => '0644',
    owner  =>  $minidplus_authentication::service_name,
    group  =>  $minidplus_authentication::service_name,
  }

  difilib::spring_boot_logrotate { $minidplus_authentication::application:
    application => $minidplus_authentication::application,
  }

  if ($platform::install_cron_jobs) {
    $log_cleanup_command = "find ${minidplus_authentication::log_root}${minidplus_authentication::application}/ -type f -name \"*.gz\" -mtime +7 -exec rm -f {} \\;"

    cron { "${minidplus_authentication::application}_log_cleanup":
      command => $log_cleanup_command,
      user    => 'root',
      hour    => '03',
      minute  => '00',
    }
  }
}
