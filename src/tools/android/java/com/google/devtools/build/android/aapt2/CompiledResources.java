// Copyright 2017 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.android.aapt2;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.devtools.build.android.ManifestContainer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Contains reference to the aapt2 generated .flat file archive and a manifest.
 *
 * <p>This represents the state between the aapt2 compile and link actions.
 */
public class CompiledResources implements ManifestContainer {

  private final Path resources;
  private final Path manifest;
  private final List<Path> assetsDirs;
  private final Optional<Path> stableIds;

  private CompiledResources(
      Path resources, Path manifest, List<Path> assetsDirs, Optional<Path> stableIds) {
    this.resources = resources;
    this.manifest = manifest;
    this.assetsDirs = assetsDirs;
    this.stableIds = stableIds;
  }

  public static CompiledResources from(Path resources, Path manifest) {
    return from(resources, manifest, ImmutableList.of());
  }

  public static CompiledResources from(
      Path resources, Path manifest, @Nullable List<Path> assetDirs) {
    return new CompiledResources(
        resources, manifest, assetDirs != null ? assetDirs : ImmutableList.of(), Optional.empty());
  }

  public Path getZip() {
    return resources;
  }

  /** Copies resources archive to a path and returns the new {@link CompiledResources} */
  public CompiledResources copyResourcesZipTo(Path destination) throws IOException {
    return new CompiledResources(
        Files.copy(resources, destination), manifest, assetsDirs, stableIds);
  }

  @Override
  public Path getManifest() {
    Preconditions.checkState(Files.exists(manifest));
    return manifest;
  }

  public List<String> getAssetsStrings() {
    return assetsDirs
        .stream()
        .map(
            p -> {
              Preconditions.checkArgument(Files.exists(p), "does not exist %s", p);
              return p;
            })
        .map(Path::toString)
        .collect(Collectors.toList());
  }

  public CompiledResources processManifest(Function<Path, Path> processManifest) {
    return new CompiledResources(resources, processManifest.apply(manifest), assetsDirs, stableIds);
  }

  public CompiledResources addStableIds(Optional<Path> stableIds) {
    return new CompiledResources(resources, manifest, assetsDirs, stableIds);
  }

  public Optional<Path> getStableIds() {
    return stableIds;
  }
}
