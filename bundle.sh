#!/bin/bash

if [ -f submission.zip ]; then
  old=$(date +%s)
  echo "Backup up old submission as submission-${old}"
  echo
  mv submission.zip submission-"${old}".zip
fi

touch refs.md

zip submission.zip src/examblock/controller/ExamBlockController.java
zip submission.zip src/examblock/model/Desk.java
zip submission.zip src/examblock/model/Exam.java
zip submission.zip src/examblock/model/ExamBlockModel.java
zip submission.zip src/examblock/model/ExamList.java
zip submission.zip src/examblock/model/Session.java
zip submission.zip src/examblock/model/SessionList.java
zip submission.zip src/examblock/model/Student.java
zip submission.zip src/examblock/model/StudentList.java
zip submission.zip src/examblock/model/Subject.java
zip submission.zip src/examblock/model/SubjectList.java
zip submission.zip src/examblock/model/Unit.java
zip submission.zip src/examblock/model/UnitList.java
zip submission.zip src/examblock/model/Venue.java
zip submission.zip src/examblock/model/VenueList.java
zip submission.zip src/examblock/view/components/DialogUtils.java
zip submission.zip src/examblock/view/components/DoubleSpinnerField.java
zip submission.zip src/examblock/view/components/FileChooser.java
zip submission.zip src/examblock/view/ExamBlockView.java

zip submission.zip test/examblock/model/SessionTest.java
zip submission.zip test/examblock/model/StudentTest.java
zip submission.zip test/examblock/model/SubjectTest.java
zip submission.zip test/examblock/model/UnitTest.java
zip submission.zip test/examblock/model/VenueTest.java

zip submission.zip refs.md
