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
        Result=analyze(SplicedList{FileInd});
        OutputFile=[strrep(SplicedList{FileInd},SplicedPath,SplicedOutputPath) '.mat'];
        [~,InputName,~]=fileparts(SplicedList{FileInd});
        %one option is to have one mask per file with the same name and
        %possibly different extension
        BinMaskPath=dir([MasksPath InputName '.*']);
        if ~isempty(BinMaskPath)
            BinMask=mean(double(imread([MasksPath BinMaskPath.name])),3)>128;
        else
            %the other is to have one mask in the entire folder, corresponding to
            %the entire dataset (such as the synthetic dataset of Fontani et al.)
            %make it a .png
            BinMaskPath=dir([MasksPath '*.png']);
            if length(BinMaskPath)>1
                error('Something is wrong with the masks');
            else
                BinMask=mean(double(imread([MasksPath BinMaskPath(1).name])),3)>128;
            end
        end
        [OutputPath,~,~]=fileparts(OutputFile);
        mkdir(OutputPath);
        save(OutputFile,'Result','AlgorithmName','BinMask','-v7.3');
    end
    
    % the ground truth mask for positive examples is taken from the root,
    % currently the square used in Fontani et al.
    BinMask=mean(double(imread('PositivesMask.png')),3)>128;
    for FileInd=1:length(AuthenticList)
        Result=analyze(AuthenticList{FileInd});
        OutputFile=[strrep(AuthenticList{FileInd},AuthenticPath,AuthenticOutputPath) '.mat'];
        [Path,~,~]=fileparts(OutputFile);
        mkdir(Path);
        save(OutputFile,'Result','AlgorithmName','BinMask','-v7.3');
    end
    
    warning('on','all');
    rmpath(['.' filesep 'Algorithms' filesep AlgorithmName]);
    
end

