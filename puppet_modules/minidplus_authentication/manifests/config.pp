#config.pp
class minidplus_authentication::config inherits minidplus_authentication {

  file { "${minidplus_authentication::install_dir}${minidplus_authentication::application}/${minidplus_authentication::artifact_id}.conf":
    ensure  => 'file',
    content => template("${module_name}/${minidplus_authentication::artifact_id}.conf.erb"),
    owner   => $minidplus_authentication::service_name,
    group   => $minidplus_authentication::service_name,
    mode    => '0444',
  } ->
  file { "${minidplus_authentication::config_dir}${minidplus_authentication::application}/application.yml":
    ensure  => 'file',
    content => template("${module_name}/application.yml.erb"),
    owner   => $minidplus_authentication::service_name,
    group   => $minidplus_authentication::service_name,
    mode    => '0444',
  } ->
  file { "/etc/rc.d/init.d/${minidplus_authentication::service_name}":
    ensure => 'link',
    target => "${minidplus_authentication::install_dir}${minidplus_authentication::application}/${minidplus_authentication::artifact_id}.war",
  }

  difilib::logback_config { $minidplus_authentication::application:
    application       => $minidplus_authentication::application,
    owner             => $minidplus_authentication::service_name,
    group             => $minidplus_authentication::service_name,
    loglevel_no       => $minidplus_authentication::log_level,
    loglevel_nondifi  => $minidplus_authentication::log_level,
  }


}
