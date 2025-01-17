/*
 * The MIT License (MIT) Copyright (c) 2020-2023 artipie.com
 * https://github.com/artipie/artipie/blob/master/LICENSE.txt
 */
package com.artipie.rpm.pkg;

import com.artipie.asto.test.TestResource;
import com.artipie.rpm.Digest;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;
import org.redline_rpm.header.Header;

/**
 * Test for {@link HeaderTags}.
 * @since 1.10
 * @checkstyle MagicNumberCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class HeaderTagsTest {

    @Test
    void readsRecommends() throws IOException {
        final Path file = new TestResource("apr-util-1.6.1-13.h1.eulerosv2r12.x86_64.rpm").asPath();
        MatcherAssert.assertThat(
            new HeaderTags(
                new FilePackage.Headers(new FilePackageHeader(file).header(), file, Digest.SHA256)
            ).dependencyNames(WeakDepsTags.RECOMMENDNAME),
            Matchers.contains("apr-util-bdb(x86-64)", "apr-util-openssl(x86-64)")
        );
    }

    @Test
    void readsRecommendsVers() throws IOException {
        final Path file = new TestResource("apr-util-1.6.1-13.h1.eulerosv2r12.x86_64.rpm").asPath();
        MatcherAssert.assertThat(
            new HeaderTags(
                new FilePackage.Headers(new FilePackageHeader(file).header(), file, Digest.SHA256)
            ).dependencyVers(WeakDepsTags.RECOMMENDVERSION).stream()
                .map(HeaderTags.Version::toString).collect(Collectors.toList()),
            Matchers.contains("1.6.1-13.h1.eulerosv2r12", "1.6.1-13.h1.eulerosv2r12")
        );
    }

    @Test
    void readsRecommendsFlags() throws IOException {
        final Path file = new TestResource("apr-util-1.6.1-13.h1.eulerosv2r12.x86_64.rpm").asPath();
        MatcherAssert.assertThat(
            new HeaderTags(
                new FilePackage.Headers(new FilePackageHeader(file).header(), file, Digest.SHA256)
            ).dependencyFlags(WeakDepsTags.RECOMMENDFLAGS),
            Matchers.contains(Optional.of("EQ"), Optional.of("EQ"))
        );
    }

    @Test
    void readsObsoletesNames() throws IOException {
        final Path file = new TestResource("ant-1.9.4-2.el7.noarch.rpm").asPath();
        MatcherAssert.assertThat(
            new HeaderTags(
                new FilePackage.Headers(new FilePackageHeader(file).header(), file, Digest.SHA256)
            ).dependencyNames(Header.HeaderTag.OBSOLETENAME),
            Matchers.contains("ant-scripts")
        );
    }

    @Test
    void readsObsoletesVer() throws IOException {
        final Path file = new TestResource("ant-1.9.4-2.el7.noarch.rpm").asPath();
        MatcherAssert.assertThat(
            new HeaderTags(
                new FilePackage.Headers(new FilePackageHeader(file).header(), file, Digest.SHA256)
            ).dependencyVers(Header.HeaderTag.OBSOLETEVERSION)
                .stream().map(HeaderTags.Version::toString).collect(Collectors.toList()),
            Matchers.contains("0:1.9.4-2.el7")
        );
    }

    @Test
    void readsObsoletesFlags() throws IOException {
        final Path file = new TestResource("ant-1.9.4-2.el7.noarch.rpm").asPath();
        MatcherAssert.assertThat(
            new HeaderTags(
                new FilePackage.Headers(new FilePackageHeader(file).header(), file, Digest.SHA256)
            ).dependencyFlags(Header.HeaderTag.OBSOLETEFLAGS),
            Matchers.contains(Optional.of("LT"))
        );
    }

    @Test
    void readsConflictsNames() throws IOException {
        final Path file = new TestResource("authconfig-6.2.8-30.el7.x86_64.rpm").asPath();
        MatcherAssert.assertThat(
            new HeaderTags(
                new FilePackage.Headers(new FilePackageHeader(file).header(), file, Digest.SHA256)
            ).dependencyNames(Header.HeaderTag.CONFLICTNAME),
            Matchers.containsInAnyOrder(
                "pam_krb5", "samba-common",
                "samba-client", "nss_ldap", "ipa-client",
                "freeipa-client", "sssd"
            )
        );
    }

    @Test
    void readsConflictsVer() throws IOException {
        final Path file = new TestResource("authconfig-6.2.8-30.el7.x86_64.rpm").asPath();
        MatcherAssert.assertThat(
            new HeaderTags(
                new FilePackage.Headers(new FilePackageHeader(file).header(), file, Digest.SHA256)
            ).dependencyVers(Header.HeaderTag.CONFLICTVERSION)
                .stream().map(HeaderTags.Version::toString).collect(Collectors.toList()),
            Matchers.containsInAnyOrder("1.49", "3.0", "3.0", "1.15.1", "254", "2.2.0", "2.2.0")
        );
    }

    @Test
    void readsConflictsFlags() throws IOException {
        final Path file = new TestResource("authconfig-6.2.8-30.el7.x86_64.rpm").asPath();
        final List<Optional<String>> res = new HeaderTags(
            new FilePackage.Headers(new FilePackageHeader(file).header(), file, Digest.SHA256)
        ).dependencyFlags(Header.HeaderTag.CONFLICTFLAGS);
        MatcherAssert.assertThat(
            "Conflicts flags list should have 7 items",
            res, Matchers.iterableWithSize(7)
        );
        MatcherAssert.assertThat(
            "Conflicts flags list should have only LT values",
            res.stream().allMatch(item -> item.get().equals("LT")),
            new IsEqual<>(true)
        );
    }

    @Test
    void readsFileFlags() throws IOException {
        final Path file = new TestResource("time-1.7-45.el7.x86_64.rpm").asPath();
        MatcherAssert.assertThat(
            new HeaderTags(
                new FilePackage.Headers(new FilePackageHeader(file).header(), file, Digest.SHA256)
            ).fileFlags(),
            new IsEqual<>(new int[] {0, 0, 2, 2, 2, 2, 2, 2})
        );
    }
}
