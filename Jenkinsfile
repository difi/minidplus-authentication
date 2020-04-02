pipelineWithMavenAndDocker {
    enableDependencyTrack = true
    verificationEnvironment = 'eid-verification2'
    stagingEnvironment = 'eid-staging'
    stagingEnvironmentType = 'puppet2'
    productionEnvironment = 'eid-production'
    gitSshKey = 'ssh.github.com'
    puppetModules = 'minidplus_authentication'
    librarianModules = 'DIFI-minidplus_authentication'
    puppetApplyList = ['eid-systest-oidc-app01 baseconfig,minidplus_authentication']
}
