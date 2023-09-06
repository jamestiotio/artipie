/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/artipie/LICENSE.txt
 */
package com.artipie.conan;

import com.artipie.asto.test.TestResource;
import com.artipie.test.ContainerResultMatcher;
import com.artipie.test.TestDeployment;
import java.io.IOException;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Integration tests for Conan repository.
 * @since 0.23
 */
@EnabledOnOs({OS.LINUX, OS.MAC})
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class ConanITCase {
    /**
     * Path prefix to conan repository test data in java resources.
     */
    private static final String SRV_RES_PREFIX = "conan/conan_server/data";

    /**
     * Path prefix for conan repository test data in artipie container repo.
     */
    private static final String SRV_REPO_PREFIX = "/var/artipie/data/my-conan";

    /**
     * Conan server zlib package files list for integration tests.
     */
    private static final String[] CONAN_TEST_PKG = {
        "zlib/1.2.13/_/_/0/package/dfbe50feef7f3c6223a476cd5aeadb687084a646/0/conaninfo.txt",
        "zlib/1.2.13/_/_/0/package/dfbe50feef7f3c6223a476cd5aeadb687084a646/0/conan_package.tgz",
        "zlib/1.2.13/_/_/0/package/dfbe50feef7f3c6223a476cd5aeadb687084a646/0/conanmanifest.txt",
        "zlib/1.2.13/_/_/0/package/dfbe50feef7f3c6223a476cd5aeadb687084a646/revisions.txt",
        "zlib/1.2.13/_/_/0/export/conan_export.tgz",
        "zlib/1.2.13/_/_/0/export/conanfile.py",
        "zlib/1.2.13/_/_/0/export/conanmanifest.txt",
        "zlib/1.2.13/_/_/0/export/conan_sources.tgz",
        "zlib/1.2.13/_/_/revisions.txt",
    };

    /**
     * Test deployments.
     * @checkstyle VisibilityModifierCheck (10 lines)
     * @checkstyle MagicNumberCheck (10 lines)
     */
    @RegisterExtension
    final TestDeployment containers = new TestDeployment(
        () -> TestDeployment.ArtipieContainer.defaultDefinition()
            .withRepoConfig("conan/conan.yml", "my-conan")
            .withExposedPorts(9301),
        () -> new TestDeployment.ClientContainer("conanio/gcc11")
            .withWorkingDirectory("/opt")
    );

    @Test
    public void incorrectPortFailTest() throws IOException {
        for (final String file : ConanITCase.CONAN_TEST_PKG) {
            this.containers.putBinaryToArtipie(
                new TestResource(
                    String.join("/", ConanITCase.SRV_RES_PREFIX, file)
                ).asBytes(),
                String.join("/", ConanITCase.SRV_REPO_PREFIX, file)
            );
        }
        this.containers.assertExec(
            "Conan remote add failed", new ContainerResultMatcher(),
            "conan remote add -f conan-test http://artipie:9300 False".split(" ")
        );
        this.containers.assertExec(
            "Conan remote add failed", new ContainerResultMatcher(
                new IsNot<>(new IsEqual<>(ContainerResultMatcher.SUCCESS))
            ),
            "conan install zlib/1.2.13@ -r conan-test -b -pr:b=default".split(" ")
        );
    }

    @Test
    public void incorrectPkgFailTest() throws IOException {
        for (final String file : ConanITCase.CONAN_TEST_PKG) {
            this.containers.putBinaryToArtipie(
                new TestResource(
                    String.join("/", ConanITCase.SRV_RES_PREFIX, file)
                ).asBytes(),
                String.join("/", ConanITCase.SRV_REPO_PREFIX, file)
            );
        }
        this.containers.assertExec(
            "Conan remote add failed", new ContainerResultMatcher(
                new IsNot<>(new IsEqual<>(ContainerResultMatcher.SUCCESS))
            ),
            "conan install zlib/1.2.11@ -r conan-test -b -pr:b=default".split(" ")
        );
    }

    @Test
    public void installFromArtipie() throws IOException {
        for (final String file : ConanITCase.CONAN_TEST_PKG) {
            this.containers.putBinaryToArtipie(
                new TestResource(
                    String.join("/", ConanITCase.SRV_RES_PREFIX, file)
                ).asBytes(),
                String.join("/", ConanITCase.SRV_REPO_PREFIX, file)
            );
        }
        this.containers.assertExec(
            "Conan remote add failed", new ContainerResultMatcher(),
            "conan install zlib/1.2.13@ -r conan-test".split(" ")
        );
    }

    @Test
    public void uploadToArtipie() throws IOException {
        this.containers.assertExec(
            "Conan install failed", new ContainerResultMatcher(),
            "conan install zlib/1.2.13@ -r conancenter".split(" ")
        );
        this.containers.assertExec(
            "Conan upload failed", new ContainerResultMatcher(),
            "conan upload zlib/1.2.13@ -r conan-test --all".split(" ")
        );
    }

    @Test
    public void uploadFailtest() throws IOException {
        this.containers.assertExec(
            "Conan upload failed", new ContainerResultMatcher(
                new IsNot<>(new IsEqual<>(ContainerResultMatcher.SUCCESS))
            ),
            "conan upload zlib/1.2.13@ -r conan-test --all".split(" ")
        );
    }

    @BeforeEach
    void init() throws IOException {
        this.containers.assertExec(
            "Conan profile init failed", new ContainerResultMatcher(),
            "conan profile new --detect default".split(" ")
        );
        this.containers.assertExec(
            "Conan profile update failed", new ContainerResultMatcher(),
            "conan profile update settings.compiler.libcxx=libstdc++11 default".split(" ")
        );
        this.containers.assertExec(
            "Conan remote add failed", new ContainerResultMatcher(),
            "conan remote add conan-test http://artipie:9301 False".split(" ")
        );
    }
}
