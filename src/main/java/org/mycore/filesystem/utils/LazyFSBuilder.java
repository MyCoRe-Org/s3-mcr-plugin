/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.filesystem.utils;

import org.mycore.filesystem.model.Directory;
import org.mycore.filesystem.model.File;
import org.mycore.filesystem.model.FileBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LazyFSBuilder {

    HashMap<String, Directory> directoryMap = new HashMap<>();

    Directory root = new Directory();
    private String virtualParent;

    public LazyFSBuilder(String virtualParent){
        this.virtualParent = virtualParent;
        root.setChildren(new ArrayList<>());
        root.setPath(virtualParent + "");
    }

    public Directory getRoot() {
        return root;
    }

    public Directory getDirectory(String path){
        if(path.equals("")){
            return root;
        }
        final String[] split = path.split("/");
        Directory current = this.root;
        for (String folder: split) {
            final Optional<FileBase> file = current.getChildren().stream().filter(child -> child.getName().equals(folder+"/")).findFirst();
            if(file.isEmpty()){
                return null;
            }

            current = (Directory) file.get();
        }

        return current;
    }

    public void addDirectory(String path){
        final String[] split = path.split("/");
        buildDirectories(root, Stream.of(split).collect(Collectors.toList()));
    }

    public void add(String path, int size, Date modified) {
        final Directory parent = buildDirectories(root, getParentFolders(path));

        final File file = new File();
        file.setName(getFileName(path));
        file.setPath(virtualParent+ path);
        file.setSize(size);
        file.setLastModified(modified);
        parent.getChildren().add(file);
    }

    private Directory buildDirectories(Directory parent, List<String> pathParts) {
        Directory fileParentDirectory = parent;
        StringBuilder pathBuilder = new StringBuilder();
        for (String pathPart : pathParts) {
            pathBuilder.append(pathPart).append('/');
            fileParentDirectory = createDirectory(pathBuilder.toString(), fileParentDirectory);
        }
        return fileParentDirectory;
    }

    private List<String> getParentFolders(String filePath) {
        String[] split = filePath.split("/");
        return Arrays.stream(split).limit(split.length - 1).collect(Collectors.toList());
    }

    private Directory createDirectory(String path, Directory parent) {
        return directoryMap.computeIfAbsent(path, (_path) -> {
            Directory directory = new Directory();
            directory.setPath(virtualParent + _path);
            directory.setName(getFileName(_path) + "/");
            directory.setChildren(new ArrayList<>());
            parent.getChildren().add(directory);
            return directory;
        });
    }

    private String getFileName(String filePath) {
        String[] split = filePath.split("/");
        List<String> pathPartsWF = Arrays.asList(split);
        return pathPartsWF.get(pathPartsWF.size() - 1);
    }
}
