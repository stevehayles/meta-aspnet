include 2.1-preview1-revs.inc
include corehost.inc

SRC_URI += "\
  file://0001-build.sh-Support-our-build-preview2.patch \
"
LIC_FILES_CHKSUM = "file://LICENSE.TXT;md5=42b611e7375c06a28601953626ab16cb"

do_configure() {
    sed -i s/arm-linux-gnueabihf/${TARGET_SYS}/g ${S}/cross/arm/toolchain.cmake
}

do_compile() {
    cd ${S}
    # Bitbake sets bindir ("/usr/bin") which MsBuild would happily pick up
    # as BinDir to store the built libraries in
    unset bindir
    ROOTFS_DIR=${STAGING_DIR_HOST} GCC_TOOLCHAIN=${STAGING_BINDIR_TOOLCHAIN} OverridePackageSource="${STAGING_DIR_HOST}/opt/dotnet-nupkg" ./build.sh \
        -ConfigurationGroup=Release \
        -TargetArchitecture=${TARGET_ARCH} \
        -PortableBuild=true \
        -SkipTests=true \
        -DisableCrossgen=true \
        -CrossBuild=true
}

do_install() {
    install -d ${D}/opt/dotnet
    cp -dr ${S}/Bin/obj/${CORE_RUNTIME_ID}.Release/combined-framework-host/* ${D}/opt/dotnet/
}

add_dotnet_symlink() {
    # create usr/local/bin if it doesn't exist
    mkdir -p -m 775 ${D}/usr/local/bin
    
    # and then create symlink into path
    ln -sf ${D}/opt/dotnet/dotnet ${D}/usr/local/bin
}

ROOTFS_POSTPROCESS_COMMAND += "add_dotnet_symlink;"


