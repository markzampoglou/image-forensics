clear all;

%The name of the algorithm. Must be the name of a subdirectory in %"Algorithms"
Options.AlgorithmName='ADQ1';
%The name of the dataset. Only used for naming the output folders, does not
%have to correspond to an existing path.
Options.DatasetName='Carv';

% Make sure all paths end with path separator! ("/" or "\" depending on your system)
% Root path of the spliced images (no problem if they are further split into subfolders): 
Options.SplicedPath='/media/marzampoglou/3TB_B/ImageForensics/Datasets/Carvalho/tifs-database/DSO-1/Sp/';
% Root path of the authentic images (no problem if they are further split into subfolders):
Options.AuthenticPath='/media/marzampoglou/3TB_B/ImageForensics/Datasets/Carvalho/tifs-database/DSO-1/Au/';
% Masks exist only for spliced images. They can be either a) placed in a
% folder structure identical to the spliced images or b) have one single
% png image in the current folder root to serve as a mask for the entire
% dataset. See README for details.
Options.MasksPath='/media/marzampoglou/3TB_B/ImageForensics/Datasets/Masks/Carvalho/tifs-database/DSO-1/Sp/';
% Subdirectories per dataset and algorithm are created automatically, so
% "OutputPath" should better be the root path for all outputs
Options.OutputPath='/media/marzampoglou/3TB_B/ImageForensics/Datasets/TmpOutput/';
% Certain algorithms (those depending on jpeg_read, like ADQ2, ADQ3 and
% NADQ) only operate on .jpg and .jpeg files.
Options.ValidExtensions={'*.jpg','*.jpeg','*.tiff','*.tif','*.png','*.bmp','*.gif'}; %{'*.jpg','*.jpeg'};

%Run the algorithm for each image in the dataset and save the results
ExtractMaps(Options);
%Estimate the output map statistics for each image, and gather them in one
%list, then estimate the TP-FP curves
Curves=CollectMapStatistics(Options);

%%%%%% Compact results to a visualizable output
PresentationCurves.Means=CompactCurve(Curves.MedianPositives,Curves.MeanThreshValues);
PresentationCurves.Medians=CompactCurve(Curves.MedianPositives,Curves.MedianThreshValues);
PresentationCurves.KS=CompactCurve(Curves.KSPositives,0:1/(size(Curves.KSPositives,2)-1):1);

figure(1);
plot(PresentationCurves.KS(2,:),PresentationCurves.KS(3,:));
axis([0 0.5 0 1]);
xlabel('False Positives');
ylabel('True Positives');
title(['Medians difference:' Options.AlgorithmName ' ' Options.DatasetName]);

Values05=PresentationCurves.KS(3,Index05(PresentationCurves.KS(2,:)>0.05));
TP_at_05=Values05(end);
disp(['True Positives at 5% False Positives: ' num2str(TP_at_05*100) '%']);