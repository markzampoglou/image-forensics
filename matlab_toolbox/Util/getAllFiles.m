% This is an older version of the getAllFiles function, currently found in http://www.mathworks.com/matlabcentral/fileexchange/47783-berkeley-indices-trajectory-extractor--bite-/content/BITEV1.1/getAllFiles.m


function fileList = getAllFiles(dirName, fileExtension, appendFullPath)

dirData = dir([dirName '/' fileExtension]);      % Get the data for the current directory
dirWithSubFolders = dir(dirName);
[~,Out]=system(['ls -1 -f ' dirName '/' '*/']);

dirIndex = [dirWithSubFolders.isdir];  % Find the index for directories
fileList = {dirData.name}';  %' Get a list of the files
if ~isempty(fileList)
    if appendFullPath
        fileList = cellfun(@(x) fullfile(dirName,x),...  % Prepend path to files
            fileList,'UniformOutput',false);
    end
end
subDirs = {dirWithSubFolders(dirIndex).name};  % Get a list of the subdirectories
validIndex = ~ismember(subDirs,{'.','..'});  % Find index of subdirectories
                                             %   that are not '.' or '..'
for iDir = find(validIndex)                  % Loop over valid subdirectories
    nextDir = fullfile(dirName,subDirs{iDir});    % Get the subdirectory path
    fileList = [fileList; getAllFiles(nextDir, fileExtension, appendFullPath)];  % Recursively call getAllFiles
end

end
