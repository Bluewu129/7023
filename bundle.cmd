@echo OFF
setlocal enabledelayedeexamDayansion

:: Backup existing submission
if not exist submission.zip goto nozip
echo - Backing up existing submission.zip

:: Get the current date and time in a consistent format
for /f "tokens=2 delims==" %%A in ('"wmic os get localdatetime /value"') do set datetime=%%A

:: Extract date and time components
set yy=!datetime:~0,4!
set mm=!datetime:~4,2!
set dd=!datetime:~6,2!
set hh=!datetime:~8,2!
set min=!datetime:~10,2!
set sec=!datetime:~12,2!

:: Format the timestamp
set timestamp=!yy!!mm!!dd!!hh!!min!!sec!

:: Rename the file
ren submission.zip submission-!timestamp!.zip

:nozip

:: See if there is an existing zip on this system
echo - Checking for an installed zip program
set zipath=
"%zipath%zip" >nul 2>nul
if %ERRORLEVEL%==0 echo   Yes, found one && goto COMMON
echo   No, none found.

:: See if we've been here before and already install gnu zip
echo - Checking if we have installed one before
set zipath=%ProgramFiles(x86)%\GnuWin32\bin\
"%zipath%zip" >nul 2>nul
if %ERRORLEVEL%==0 echo   Yes, we have && goto COMMON

echo   No, we haven't. Let's install one.
echo.
echo You be be asked to allow "download.exe" to run.
echo Please click on YES when it asks for permission.
echo.
pause

winget install gnuwin32.zip

:COMMON
"%zipath%zip" submission.zip src/examblock/controller/ExamBlockController.java
"%zipath%zip" submission.zip src/examblock/model/Desk.java
"%zipath%zip" submission.zip src/examblock/model/Exam.java
"%zipath%zip" submission.zip src/examblock/model/ExamBlockModel.java
"%zipath%zip" submission.zip src/examblock/model/ExamList.java
"%zipath%zip" submission.zip src/examblock/model/Session.java
"%zipath%zip" submission.zip src/examblock/model/SessionList.java
"%zipath%zip" submission.zip src/examblock/model/Student.java
"%zipath%zip" submission.zip src/examblock/model/StudentList.java
"%zipath%zip" submission.zip src/examblock/model/Subject.java
"%zipath%zip" submission.zip src/examblock/model/SubjectList.java
"%zipath%zip" submission.zip src/examblock/model/Unit.java
"%zipath%zip" submission.zip src/examblock/model/UnitList.java
"%zipath%zip" submission.zip src/examblock/model/Venue.java
"%zipath%zip" submission.zip src/examblock/model/VenueList.java
"%zipath%zip" submission.zip src/examblock/view/components/DialogUtils.java
"%zipath%zip" submission.zip src/examblock/view/components/DoubleSpinnerField.java
"%zipath%zip" submission.zip src/examblock/view/components/FileChooser.java
"%zipath%zip" submission.zip src/examblock/view/ExamBlockView.java

"%zipath%zip" submission.zip test/examblock/model/SessionTest.java
"%zipath%zip" submission.zip test/examblock/model/StudentTest.java
"%zipath%zip" submission.zip test/examblock/model/SubjectTest.java
"%zipath%zip" submission.zip test/examblock/model/UnitTest.java
"%zipath%zip" submission.zip test/examblock/model/VenueTest.java

"%zipath%zip" submission.zip refs.md

:DONE
endlocal
