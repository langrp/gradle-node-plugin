
tasks.withType<Wrapper> {
	distributionType = Wrapper.DistributionType.BIN
	gradleVersion = "7.3.3"
}

allprojects {
	group = "com.palawanframe.sample"
	version = "1.0.0-SNAPSHOT"
}
