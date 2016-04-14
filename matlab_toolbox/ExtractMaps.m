function ExtractMaps( Options )
    AlgorithmName=Options.AlgorithmName;
    DatasetName=Options.DatasetName;
    SplicedPath=Options.SplicedPath;
    AuthenticPath=Options.AuthenticPath;
    MasksPath=Options.MasksPath;
    
    SplicedOutputPath=[Options.OutputPath DatasetName AlgorithmName filesep 'Sp' filesep];
    AuthenticOutputPath=[Options.OutputPath DatasetName AlgorithmName filesep 'Au' filesep];
    ValidExtensions=Options.ValidExtensions;
    
    SplicedList={};
    AuthenticList={};
    for Ext=1:length(ValidExtensions)
        SplicedList=[SplicedList;getAllFiles(SplicedPath,ValidExtensions{Ext},true)];
        AuthenticList=[AuthenticList;getAllFiles(AuthenticPath,ValidExtensions{Ext},true)];
    end
    warning('off','MATLAB:MKDIR:DirectoryExists');
    
    addpath(['.' filesep 'Algorithms' filesep AlgorithmName]);
    for FileInd=1:length(SplicedList)
        OutputFile=[strrep(SplicedList{FileInd},SplicedPath,SplicedOutputPath) '.mat'];
        % If the .mat file already exists, skip it. This allows for partial
        % batch extraction. Remove if you intend to overwrite existing files
        if ~exist(OutputFile,'file')
            Result=analyze(SplicedList{FileInd});
            [~,InputName,~]=fileparts(SplicedList{FileInd});
            %one option is to have one mask per file with the same name and
            %possibly different extension
            BinMaskPath=dir([MasksPath InputName '.*']);
            if ~isempty(BinMaskPath)
                Mask=mean(double(imread([MasksPath BinMaskPath.name])),3);
                MaskMin=min(Mask(:));
                MaskMax=max(Mask(:));
                MaskThresh=MaskMin+MaskMax/2;
                BinMask=Mask>MaskThresh;
            else
                %the other is to have one mask in the entire folder, corresponding to
                %the entire dataset (such as the synthetic dataset of Fontani et al.)
                %make it a .png
                BinMaskPath=dir([MasksPath '*.png']);
                if length(BinMaskPath)>1
                    error('Something is wrong with the masks');
                else
                    Mask=mean(double(CleanUpImage([MasksPath BinMaskPath(1).name])),3);
                    MaskMin=min(Mask(:));
                    MaskMax=max(Mask(:));
                    MaskThresh=MaskMin+MaskMax/2;
                    BinMask=Mask>MaskThresh;
                end
            end
            [OutputPath,~,~]=fileparts(OutputFile);
            mkdir(OutputPath);
            save(OutputFile,'Result','AlgorithmName','BinMask','-v7.3');
        end
    end
    
    % the ground truth mask for positive examples is taken from the root,
    % currently the square used in Fontani et al.
    BinMask=mean(double(CleanUpImage('PositivesMask.png')),3)>128;
    for FileInd=1:length(AuthenticList)
        OutputFile=[strrep(AuthenticList{FileInd},AuthenticPath,AuthenticOutputPath) '.mat'];
        % If the .mat file already exists, skip it. This allows for partial
        % batch extraction. Remove if you intend to overwrite existing files
        if ~exist(OutputFile,'file')
            Result=analyze(AuthenticList{FileInd});
            [Path,~,~]=fileparts(OutputFile);
            mkdir(Path);
            save(OutputFile,'Result','AlgorithmName','BinMask','-v7.3');
        end
    end
    
    warning('on','all');
    rmpath(['.' filesep 'Algorithms' filesep AlgorithmName]);
end