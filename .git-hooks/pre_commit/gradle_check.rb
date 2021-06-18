# frozen_string_literal: true

# This file is part of the Rsync for Tasker project.
# Copyright © 2021 by the authors - see the AUTHORS file for details.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

module Overcommit
  module Hook
    module PreCommit
      class GradleCheck < Base
        def run
          result = execute(command)

          if result.success?
            :pass
          else
            [:fail, result.stdout + result.stderr]
          end
        end
      end
    end
  end
end
