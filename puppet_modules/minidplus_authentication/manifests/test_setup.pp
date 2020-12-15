class minidplus_authentication::test_setup inherits minidplus_authentication {

  if ($platform::test_setup) {

    file { "${minidplus_authentication::config_dir}${minidplus_authentication::application}/minidauth-env.jks":
      ensure => 'file',
      source => "puppet:///modules/${caller_module_name}/minidauth-env.jks",
      group  => $minidplus_authentication::service_name,
      owner  => $minidplus_authentication::service_name,
      mode   => '0644',
      notify => Class['minidplus_authentication::Service'],
    }
  }

}
