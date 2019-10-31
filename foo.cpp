#include<iostream>

#include<bitset>

#include<opencv2/opencv.hpp>

#include<stdlib.h>

#include<fstream>

 

#define OPTIONAL_PROC

#define MEASURE_MARGIN 20

 

using namespace std;

using namespace cv;

 

typedef struct _braille{

	_braille() : rect(), value(0), index(0){}

	virtual ~_braille(){}

	Rect rect;

	int value;

	int index;

}braille;

class Foo{

	public:

	void bar() {

		Mat inputImg;

		string tl[12];

		string a = {};

		static char ch[100];

		ofstream out;

		inputImg = imread("input.png", IMREAD_GRAYSCALE);

		//imshow("input", inputImg);

		

		Mat thresholdImg;

		adaptiveThreshold(inputImg, thresholdImg, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, 21, 10);

		

		Mat morphologyElement3x3 = getStructuringElement(MORPH_RECT, Size(3, 3));

		GaussianBlur(thresholdImg, thresholdImg, Size(3, 3), 0);

		erode(thresholdImg, thresholdImg, morphologyElement3x3);

		threshold(thresholdImg, thresholdImg, 21, 255, CV_THRESH_BINARY);

		//imshow("threshold", thresholdImg);

		

		Mat measureImg = Mat(Size(thresholdImg.cols + MEASURE_MARGIN, thresholdImg.rows + MEASURE_MARGIN), CV_8UC1, 255);

		thresholdImg.copyTo(measureImg(Rect(MEASURE_MARGIN / 2, MEASURE_MARGIN / 2, thresholdImg.cols, thresholdImg.rows)));

		resize(measureImg, measureImg, measureImg.size() * 2);

		//imshow("measure", measureImg);

		

	#ifdef OPTIONAL_PROC

		Mat deskewImg = measureImg;

		vector<Point> points;

		for (int i = 0; i < deskewImg.cols; ++i){

			for (int j = 0; j < deskewImg.rows; ++j){

				if (!deskewImg.at<uchar>(Point(i, j))){

					points.push_back(Point(i, j));

				}

			}

		}

		RotatedRect box = minAreaRect(Mat(points));

		double angle = box.angle;

		if (angle < -45.0)

			angle += 90.0;

		cout << "angle : " << angle << endl;

		

		Mat rotatedImg = getRotationMatrix2D(box.center, angle, 1);

		warpAffine(deskewImg, deskewImg, rotatedImg, deskewImg.size(), INTER_CUBIC);

		//imshow("deskew", deskewImg);

	#endif

		

		SimpleBlobDetector::Params params;

		params.filterByArea = true;

		params.minArea = 2.0f * 2.0f;

		params.maxArea = 20.0f * 20.0f;

		

		Ptr<SimpleBlobDetector> blobDetector = SimpleBlobDetector::create(params);

		vector<KeyPoint> keypoints;

		blobDetector->detect(measureImg, keypoints);

		

		if (keypoints.empty()){

			cout << "there is no braille existance condition" << endl;

			//return;

		}

		

		Mat detectedImg = Mat(measureImg.size(), CV_8UC3);

		drawKeypoints(measureImg, keypoints, detectedImg, Scalar(0, 0, 255), DrawMatchesFlags::DRAW_RICH_KEYPOINTS);

		//imshow("detected", detectedImg);

		

		float blobSize = 0.0f;

		for (int i = 0; i < keypoints.size(); ++i){

			blobSize += keypoints[i].size;

		}

		blobSize /= keypoints.size();

		cout << "mean of the blob size : " << blobSize << endl;

		vector<int> coordinateX;

		vector<int> coordinateY;

		for (int i = 0; i < keypoints.size(); ++i){

			bool isNew = true;

			for (vector<int>::iterator iter = coordinateX.begin(); iter < coordinateX.end(); ++iter){

				if(abs(*iter - keypoints[i].pt.x) < blobSize){

					isNew = false;

					break;

				}

			}

			if (isNew){

				coordinateX.push_back((int)keypoints[i].pt.x);

			}

			isNew = true;

			for (vector<int>::iterator iter = coordinateY.begin(); iter < coordinateY.end(); ++iter){

				if(abs(*iter - keypoints[i].pt.y) < blobSize){

					isNew = false;

					break;

				}

			}

			if (isNew){

				coordinateY.push_back((int)keypoints[i].pt.y);

			}

		}

		sort(coordinateX.begin(), coordinateX.end());

		sort(coordinateY.begin(), coordinateY.end());

		

	#ifdef OPTIONAL_PROC

		Mat coordinateImg = detectedImg.clone();

		for (int i = 0; i < coordinateX.size(); ++i){

			line(coordinateImg, Point(coordinateX[i], 0), Point(coordinateX[i], coordinateImg.rows), Scalar(255, 0, 0));

		}

		for (int i = 0; i < coordinateY.size(); ++i){

			line(coordinateImg, Point(0, coordinateY[i]), Point(coordinateImg.cols, coordinateY[i]), Scalar(255, 0, 0));

		}

		//imshow("coordinate", coordinateImg);

	#endif 

		

		for (int i = 0; i < keypoints.size(); ++i){

			int ditanceX = detectedImg.cols / 2;

			int ditanceY = detectedImg.rows / 2;

			int tempX = 0;

			int tempY = 0;

			for (int j = 0; j < coordinateX.size(); ++j){

				if (ditanceX > abs(keypoints[i].pt.x - coordinateX[j])){

					ditanceX = abs(keypoints[i].pt.x - coordinateX[j]);

					tempX = coordinateX[j];

				}

			}

			keypoints[i].pt.x = tempX;

			

			for (int j = 0; j < coordinateY.size(); ++j){

				if (ditanceY > abs(keypoints[i].pt.y - coordinateY[j])){

					ditanceY = abs(keypoints[i].pt.y - coordinateY[j]);

					tempY = coordinateY[j];

				}

			}

			keypoints[i].pt.y = tempY;

		}

		

		Mat editedImg = Mat(detectedImg.size(), CV_8UC1);

		editedImg.setTo(255);

		for (int i = 0; i < keypoints.size(); ++i){

			circle(editedImg, Point(keypoints[i].pt.x, keypoints[i].pt.y), blobSize / 2, Scalar(0), -1, LINE_AA);

		}

		//imshow("edited", editedImg);

		

	#ifdef OPTIONAL_PROC

		Mat editedwithLineImg = editedImg.clone();

		for (int i = 0; i < coordinateX.size(); ++i){

			line(editedwithLineImg, Point(coordinateX[i], 0), Point(coordinateX[i], editedwithLineImg.rows), Scalar(0));

		}

		for (int i = 0; i < coordinateY.size(); ++i){

			line(editedwithLineImg, Point(0, coordinateY[i]), Point(editedwithLineImg.cols, coordinateY[i]), Scalar(0));

		}

		//imshow("editedwithLine", editedwithLineImg);

	#endif

		

		int startXPos = 0;

		int index = 0;

		vector<braille> brailleSet;

		Mat segmentationImg = Mat(editedImg.size(), CV_8UC3);

		cvtColor(editedImg, segmentationImg, CV_GRAY2BGR);

		if ((coordinateX[1] - coordinateX[0]) > (coordinateX[2] - coordinateX[1])){

			startXPos = 1;

		}

		for (int i = 0; i < coordinateY.size() - 2; i += 3){

			for (int j = startXPos; j < coordinateX.size() - 1; j += 2) {

				braille tempBraille;

				Rect rect = Rect(Point(coordinateX[j] - blobSize / 2, coordinateY[i] - blobSize / 2),

					Point(coordinateX[j+1] + blobSize / 2, coordinateY[i+2] + blobSize / 2));

				int value = 0;

				rectangle(segmentationImg, rect, Scalar(0, 0, 255));

				

				for (int k = 0; k < 2; ++k){

					for (int l = 0; l < 3; ++l){

						if (editedImg.at<uchar>(Point((int)coordinateX[j + k], (int)coordinateY[i + l])) == 0){

							value++;

						}

						value = value << 1;

					}

				}

				value = value >> 1;

				tempBraille.rect = rect;

				tempBraille.index = index++;

				tempBraille.value = value;

				brailleSet.push_back(tempBraille);

			}

		}

		if (brailleSet.empty()){

			cout << "there is no braille set!!" << endl;

			//return;

		}

		//imshow("segmentation", segmentationImg);

		

	#ifdef OPTIONAL_PROC

		Mat compareImg, resizedImg;

		cvtColor(inputImg, compareImg, CV_GRAY2BGR);

		resize(segmentationImg, resizedImg, segmentationImg.size() / 2);

		addWeighted(compareImg, 0.8, resizedImg(Rect(MEASURE_MARGIN / 2, MEASURE_MARGIN / 2, inputImg.cols, inputImg.rows)), 0.2, 0.0, compareImg);

		//imshow("compare", compareImg);

	#endif

	

		Mat resultImg = Mat(Size(segmentationImg.size()), CV_8UC3);

		resultImg.setTo(255);

		addWeighted(resultImg, 0.8, segmentationImg, 0.2, 0.0, resultImg);

		

		int intFontFace = CV_FONT_HERSHEY_SIMPLEX;

		double dblFontScale = brailleSet[0].rect.size().width / 60.0;

		int intFontThickness = (int)std::round(dblFontScale * 2);

		

		for (int i = 0; i < brailleSet.size(); ++i){

			Point center, bottomLeft;

			center = (brailleSet[i].rect.tl() + brailleSet[i].rect.br()) / 2;

			center.x -= getTextSize(to_string(brailleSet[i].value), intFontFace, dblFontScale, intFontThickness, 0).width / 2;

			center.y += getTextSize(to_string(brailleSet[i].value), intFontFace, dblFontScale, intFontThickness, 0).height / 2;

			

			bottomLeft = Point(brailleSet[i].rect.tl().x, brailleSet[i].rect.br().y);

			bottomLeft.x -= blobSize / 2;

			bottomLeft.y += getTextSize(bitset<6>(brailleSet[i].value).to_string(), intFontFace, dblFontScale * 0.7, intFontThickness * 0.7, 0).height / 2 +blobSize / 2;

			

			putText(resultImg, to_string(brailleSet[i].value), center, intFontFace, dblFontScale, Scalar(255, 0, 0), intFontThickness);

			putText(resultImg, bitset<6>(brailleSet[i].value).to_string(), bottomLeft, intFontFace, dblFontScale * 0.7, Scalar(0, 0, 0), intFontThickness * 0.7);

		}

		//imshow("result", resultImg);

		for (int i = 0; i < brailleSet.size(); ++i){

			tl[i] = bitset<6>(brailleSet[i].value).to_string();

		}

		for (int i =0; i< brailleSet.size(); ++i){

			a = a + tl[i] + " ";

		}

		strcpy(ch, a.c_str());

		//vector<string> v(begin(tl), end(tl));

		//for(auto &x: v)

			//cout << x << " ";

		

		cout << ch << endl;

		out.open("output.txt");

		out << ch;

		out.close();

	} 

};

 

extern "C" {

	Foo* Foo_new(){return new Foo();}

	void Foo_bar(Foo* foo){foo->bar();}

}